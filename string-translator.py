#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import json
import os
import requests
import time
import xml.etree.ElementTree as ET
from babel import Locale
from colorama import init, Fore, Style
from pathlib import Path
from tqdm import tqdm
from typing import Dict, List, Set, Optional, Tuple

# 初始化 colorama
init(autoreset=True)

# ==================== API 配置 ====================
API_KEY = "ollama"
BASE_URL = "http://127.0.0.1:11434/v1"
MODEL = "gemma3:12b"

# 配置
RETRY_TIMES = 3  # 失败重试次数
RETRY_DELAY = 2  # 重试延迟(秒)

# ==================== 脚本配置 ====================
WORK_DIR = "app/src/main/res"
SOURCE_FILE = "values/strings.xml"

iso_languages = [
    "de",
    "en",
    "es",
    "fr",
    "ja",
    "ko",
    "pt-BR",
    "ru",
    "tr",
    "vi",
    "zh-Hans",
    "zh-Hant"
]

TARGET_LANGUAGES = iso_languages
EXCLUDE_LANGUAGES = ["zh-CN"]


def get_language_name(iso_code: str) -> str:
    try:
        return Locale.parse(iso_code, sep='-').get_display_name()
    except:
        return iso_code.upper()


def iso_to_android_dir(iso_code: str) -> str:
    """ISO语言代码转Android资源目录名"""
    if '-' in iso_code:
        lang, region = iso_code.split('-')
        return f"values-{lang.lower()}-r{region.upper()}"
    else:
        return f"values-{iso_code.lower()}"


def android_dir_to_iso(android_dir: str) -> Optional[str]:
    """Android资源目录名转ISO语言代码"""
    if not android_dir.startswith("values-"):
        return None
    parts = android_dir.replace("values-", "").split("-r")
    if len(parts) == 2:
        return f"{parts[0]}-{parts[1]}"
    elif len(parts) == 1:
        return parts[0]
    return None


# ==================== XML 解析 / inner-xml 处理 ====================
def get_element_inner_xml(elem: ET.Element) -> str:
    """获取element的inner XML(保留子节点与tail)"""
    parts = []
    if elem.text:
        parts.append(elem.text)
    for child in elem:
        parts.append(ET.tostring(child, encoding='unicode'))
        if child.tail:
            parts.append(child.tail)
    return ''.join(parts)


def set_element_inner_xml(elem: ET.Element, inner_xml: str):
    """将inner_xml字符串注入到elem中,保留XML标签结构"""
    try:
        wrapper = f"<root>{inner_xml}</root>"
        root = ET.fromstring(wrapper)
        elem.clear()
        elem.text = root.text
        for child in list(root):
            elem.append(child)
    except ET.ParseError:
        # 回退为纯文本
        elem.clear()
        elem.text = inner_xml


def parse_strings_xml(xml_path: str) -> Tuple[Dict[str, str], Set[str]]:
    """解析strings.xml文件"""
    if not os.path.exists(xml_path):
        return {}, set()

    try:
        tree = ET.parse(xml_path)
        root = tree.getroot()

        strings_dict = {}
        non_translatable_keys = set()

        for string_elem in root.findall('string'):
            name = string_elem.get('name')
            value = get_element_inner_xml(string_elem)
            translatable = string_elem.get('translatable', 'true')

            if name:
                strings_dict[name] = value
                if translatable.lower() == 'false':
                    non_translatable_keys.add(name)

        return strings_dict, non_translatable_keys
    except Exception as e:
        print(f"{Fore.RED}✗ 解析XML失败: {xml_path}")
        print(f"{Fore.RED}  错误: {str(e)}")
        return {}, set()


def write_strings_xml(xml_path: str, strings_dict: Dict[str, str],
                      source_order: List[str] = None):
    """写入strings.xml,保持源文件顺序"""
    os.makedirs(os.path.dirname(xml_path), exist_ok=True)

    existing_dict, _ = parse_strings_xml(xml_path) if os.path.exists(xml_path) else ({}, set())
    existing_dict.update(strings_dict)

    root = ET.Element('resources')

    # 使用源文件顺序
    if source_order:
        for name in source_order:
            if name in existing_dict:
                string_elem = ET.SubElement(root, 'string')
                string_elem.set('name', name)
                value = existing_dict[name] or ""
                if '<' in value and '>' in value:
                    set_element_inner_xml(string_elem, value)
                else:
                    string_elem.text = value

        # 添加额外的键(不在源顺序中)
        extra_keys = set(existing_dict.keys()) - set(source_order)
        for name in sorted(extra_keys):
            string_elem = ET.SubElement(root, 'string')
            string_elem.set('name', name)
            value = existing_dict[name] or ""
            if '<' in value and '>' in value:
                set_element_inner_xml(string_elem, value)
            else:
                string_elem.text = value
    else:
        # 默认按字母顺序
        for name in sorted(existing_dict.keys()):
            string_elem = ET.SubElement(root, 'string')
            string_elem.set('name', name)
            value = existing_dict[name] or ""
            if '<' in value and '>' in value:
                set_element_inner_xml(string_elem, value)
            else:
                string_elem.text = value

    tree = ET.ElementTree(root)
    ET.indent(tree, space="    ")

    with open(xml_path, 'wb') as f:
        f.write(b'<?xml version="1.0" encoding="utf-8"?>\n')
        tree.write(f, encoding='utf-8', xml_declaration=False)


# ==================== 翻译输出清理 ====================
def clean_model_output(model_output: str, original_text: str) -> str:
    """清理模型输出,去除多余格式"""
    if model_output is None:
        return ""
    s = model_output.strip()
    s = s.replace("'", "\\'")
    return s


# ==================== 翻译器 ====================
class ChatGPTTranslator:
    def __init__(self, api_key: str, base_url: str, model: str):
        self.api_key = api_key
        self.base_url = base_url.rstrip('/')
        self.model = model
        self.session = requests.Session()
        self.session.headers.update({
            'Authorization': f'Bearer {api_key}',
            'Content-Type': 'application/json'
        })

    def get_system_prompt(self, target_language: str, target_language_name: str) -> str:
        return f"""你是Android string本地化翻译专家。
请自动识别输入文本语言,并将其翻译为{target_language_name}({target_language})

输入说明:
- 输入为JSON,包含key(参考)和value
- 只对value进行翻译

输出规则:
- 只返回翻译后的value内容
- 不返回key
- 不包含任何额外说明

保留规则:
- 所有格式化占位符必须保持不变(如%s、%d、%1$s等)

翻译要求:
- 严格按照原文意思翻译，使用最高翻译质量。
""".strip()

    def translate(self, text: str, key: str, target_language: str,
                  target_language_name: str, retry: int = 0) -> Optional[str]:
        """翻译单个字符串,支持重试"""
        try:
            system_prompt = self.get_system_prompt(target_language, target_language_name)
            messages = [
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": json.dumps(
                    {"key": key, "value": text}, ensure_ascii=False)},
            ]

            payload = {
                "model": self.model,
                "messages": messages,
                "temperature": 0.2,
                "top_p": 0.9,
                "stream": False,
            }

            response = self.session.post(
                f"{self.base_url}/chat/completions",
                json=payload,
                timeout=30
            )

            if response.status_code == 200:
                result = response.json()
                content = result['choices'][0]['message']['content']
                translated = clean_model_output(content, text)
                return translated
            else:
                raise Exception(f"API错误: {response.status_code}")

        except Exception as e:
            if retry < RETRY_TIMES:
                time.sleep(RETRY_DELAY)
                return self.translate(text, key, target_language, target_language_name, retry + 1)
            else:
                print(f"{Fore.RED}✗ 翻译失败(已重试{RETRY_TIMES}次): {key} - {str(e)}")
                return None

    def translate_batch(self, strings_dict: Dict[str, str],
                        target_language: str,
                        target_language_name: str) -> Dict[str, str]:
        """批量翻译(串行处理)"""
        translated_dict = {}

        print(f"\n{Fore.CYAN}{'=' * 60}")
        print(f"{Fore.CYAN}开始翻译到: {Fore.YELLOW}{target_language_name} ({target_language})")
        print(f"{Fore.CYAN}待翻译数量: {Fore.YELLOW}{len(strings_dict)}")
        print(f"{Fore.CYAN}{'=' * 60}\n")

        with tqdm(total=len(strings_dict),
                  desc=f"{Fore.GREEN}翻译进度",
                  bar_format='{l_bar}{bar}| {n_fmt}/{total_fmt} [{elapsed}<{remaining}]',
                  colour='green') as pbar:

            for key, value in strings_dict.items():
                translated = self.translate(value, key, target_language, target_language_name)
                if translated:
                    translated_dict[key] = translated
                else:
                    translated_dict[key] = value
                pbar.update(1)

        success_rate = len([v for v in translated_dict.values() if v]) / len(
            strings_dict) * 100 if strings_dict else 100
        print(f"\n{Fore.GREEN}✓ 翻译完成! 成功率: {success_rate:.1f}%\n")

        return translated_dict


# ==================== 主程序 ====================
def main():
    print(f"\n{Fore.CYAN}{Style.BRIGHT}{'=' * 60}")
    print(f"{Fore.CYAN}{Style.BRIGHT}Android String.xml 批量翻译工具(改进版)")
    print(f"{Fore.CYAN}{Style.BRIGHT}{'=' * 60}\n")

    res_dir = Path(WORK_DIR)
    if not res_dir.exists():
        print(f"{Fore.RED}✗ 错误: 工作目录不存在: {WORK_DIR}")
        return

    source_xml = res_dir / SOURCE_FILE
    if not source_xml.exists():
        print(f"{Fore.RED}✗ 错误: 源文件不存在: {source_xml}")
        return

    print(f"{Fore.CYAN}► 解析源文件: {Fore.YELLOW}{SOURCE_FILE}")
    source_strings, non_translatable_keys = parse_strings_xml(str(source_xml))

    translatable_strings = {k: v for k, v in source_strings.items()
                            if k not in non_translatable_keys}

    # 获取源文件顺序
    source_order = [k for k in source_strings.keys()
                    if k not in non_translatable_keys]

    print(f"{Fore.GREEN}✓ 成功解析 {len(source_strings)} 个字符串")
    if non_translatable_keys:
        print(f"{Fore.YELLOW}⊘ 跳过 {len(non_translatable_keys)} 个不可翻译的字符串")
    print(f"{Fore.CYAN}► 实际需翻译: {Fore.GREEN}{len(translatable_strings)} 个字符串\n")

    translator = ChatGPTTranslator(API_KEY, BASE_URL, MODEL)

    target_langs = [lang for lang in TARGET_LANGUAGES
                    if lang not in EXCLUDE_LANGUAGES]

    print(f"{Fore.CYAN}► 目标语言列表:")
    for lang in target_langs:
        lang_name = get_language_name(lang)
        print(f"  {Fore.YELLOW}• {lang} ({lang_name}) -> {iso_to_android_dir(lang)}")
    print()

    for lang in target_langs:
        android_dir = iso_to_android_dir(lang)
        target_xml = res_dir / android_dir / "strings.xml"
        lang_name = get_language_name(lang)

        existing_strings, _ = parse_strings_xml(str(target_xml)) if target_xml.exists() else ({},
                                                                                              set())

        missing_keys = set(translatable_strings.keys()) - set(existing_strings.keys())
        if not missing_keys:
            print(f"{Fore.GREEN}✓ {lang} ({lang_name}): 无需翻译,已是最新")
            continue

        missing_strings = {k: translatable_strings[k] for k in missing_keys}

        translated_strings = translator.translate_batch(
            missing_strings, lang, lang_name
        )

        print(f"{Fore.CYAN}► 写入翻译结果到: {Fore.YELLOW}{target_xml}")
        write_strings_xml(str(target_xml), translated_strings, source_order)
        print(f"{Fore.GREEN}✓ 成功写入 {len(translated_strings)} 个字符串\n")

    print(f"\n{Fore.GREEN}{Style.BRIGHT}{'=' * 60}")
    print(f"{Fore.GREEN}{Style.BRIGHT}✓ 所有翻译任务完成!")
    print(f"{Fore.GREEN}{Style.BRIGHT}{'=' * 60}\n")


if __name__ == "__main__":
    main()
