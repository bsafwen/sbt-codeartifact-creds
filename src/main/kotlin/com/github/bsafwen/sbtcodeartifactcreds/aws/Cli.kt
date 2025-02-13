package com.github.bsafwen.sbtcodeartifactcreds.aws

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

fun findAwsInPath(): String? {
    val pathDirs = System.getenv("PATH").split(File.pathSeparator)
    val executable = if (System.getProperty("os.name").lowercase().contains("windows")) "aws.exe" else "aws"

    return pathDirs.map { File(it, executable) }
        .find { it.exists() && it.canExecute() }
        ?.absolutePath
}

fun runAwsCommand(vararg args: String): Pair<Int, String> {
    val awsPath = findAwsInPath() ?: throw IllegalStateException("AWS CLI not found in PATH")
    val process = ProcessBuilder(awsPath, *args)
        .redirectErrorStream(true)
        .start()

    val output = BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
        reader.lineSequence().joinToString("\n")
    }

    val exitCode = process.waitFor()
    return Pair(exitCode, output)
}
