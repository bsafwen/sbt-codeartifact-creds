package com.github.bsafwen.sbtcodeartifactcreds.aws

import com.intellij.execution.configurations.GeneralCommandLine
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

fun runAwsCommand(awsPath: String, vararg args: String): Pair<Int, String> {
    val process = ProcessBuilder(awsPath, *args)
        .redirectErrorStream(true)
        .start()

    val output = BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
        reader.lineSequence().joinToString("\n")
    }

    val exitCode = process.waitFor()
    return Pair(exitCode, output)
}
