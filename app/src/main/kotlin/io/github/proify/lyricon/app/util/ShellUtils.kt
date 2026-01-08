/*
 * Copyright 2026 Proify
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("unused")

package io.github.proify.lyricon.app.util

object ShellUtils {
    fun execCmd(
        commands: Array<String>?,
        isRoot: Boolean = false,
        isNeedResultMsg: Boolean = false
    ): CommandResult {
        if (commands.isNullOrEmpty()) return CommandResult(-1, "", "")

        return try {
            val p = ProcessBuilder(if (isRoot) "su" else "sh").start()
            p.outputStream.bufferedWriter().use { w ->
                commands.forEach { if (it.isNotEmpty()) w.write("$it\n") }
                w.write("exit\n")
            }
            val code = p.waitFor()
            if (isNeedResultMsg) {
                val out = p.inputStream.bufferedReader().readText().trim()
                val err = p.errorStream.bufferedReader().readText().trim()
                CommandResult(code, out, err)
            } else {
                CommandResult(code, "", "")
            }
        } catch (e: Exception) {
            CommandResult(-1, "", e.message ?: "Unknown Error")
        }
    }

    fun execCmd(
        command: String,
        isRoot: Boolean = false,
        isNeedResultMsg: Boolean = false
    ): CommandResult =
        execCmd(arrayOf(command), isRoot, isNeedResultMsg)

    fun execCmd(
        commands: List<String>?,
        isRoot: Boolean = false,
        isNeedResultMsg: Boolean = false
    ): CommandResult =
        execCmd(commands?.toTypedArray(), isRoot, isNeedResultMsg)

    data class CommandResult(
        val result: Int,
        val successMsg: String,
        val errorMsg: String
    )
}