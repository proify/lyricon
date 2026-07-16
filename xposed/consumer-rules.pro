#
# 已知问题：
#   io.github.libxposed.api.XposedInterface.HookBuilder.intercept 方法
#   不可以使用 Lambda 表达式创建，否则 R8 会对字节码进行异常优化，
#   导致运行时 hook 失败。
#
# 解决方案：
#   1. 所有使用 Xposed API 的地方统一使用匿名内部类替代 Lambda
#   2. 保留所有 Xposed hook 入口类及其成员，防止被混淆或优化
#
# ============================================================

# 保留 Xposed hook 相关类（防止混淆和 R8 优化）
-keep class io.github.proify.lyricon.xposed.hook.** { *; }
-keep class io.github.proify.lyricon.xposed.systemui.hook.** { *; }

# libxposed API 102 官方 R8 规则：保留入口构造器，并在入口类混淆时同步资源内容
-adaptresourcefilecontents META-INF/xposed/java_init.list
-keep,allowoptimization,allowobfuscation public class * extends io.github.libxposed.api.XposedModule {
    public <init>();
}

# 忽略 Xposed 框架的警告（避免因注解缺失导致构建中断）
-dontwarn io.github.libxposed.annotation.**
-dontwarn io.github.libxposed.api.**
-dontwarn javax.annotation.Nullable
