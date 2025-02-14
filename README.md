# sbt-codeartifact-creds

![Build](https://github.com/bsafwen/sbt-codeartifact-creds/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/26533-sbt-codeartifact-creds.svg)](https://plugins.jetbrains.com/plugin/26533-sbt-codeartifact-creds)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/26533-sbt-codeartifact-creds.svg)](https://plugins.jetbrains.com/plugin/26533-sbt-codeartifact-creds)

<!-- Plugin description -->
Helps passing the AWS CodeArtifact authentication token to Intellij.
<!-- Plugin description end -->

## How it works
For CI it is easy to pass the CodeArtifact to sbt token by exporting it. But there is no easy way
to do the same when building from Intellij. One solution would to be launch the IDE from the shell
that have the token exported. Another solution, which is the proposed solution in this repo, is to
to store the token in a file, and read it in build.sbt.

for example:
```sbt
ThisBuild / credentials ++= Seq(
  Credentials(
    "Repo Name",
    "AWS CodeArtifact Endpoint: *.codeartifact.*.amazonaws.com",
    "aws",
    sys.env.getOrElse("CODEARTIFACT_AUTH_TOKEN", scala.io.Source.fromFile(System.getProperty("user.home") + "/.sbt/.credentials").mkString)
  )
)
```

## Installation

- Using the IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "sbt-codeartifact-creds"</kbd> >
  <kbd>Install</kbd>
  
- Using JetBrains Marketplace:

  Go to [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/26533-sbt-codeartifact-creds) and install it by clicking the <kbd>Install to ...</kbd> button in case your IDE is running.

  You can also download the [latest release](https://plugins.jetbrains.com/plugin/26533-sbt-codeartifact-creds/versions) from JetBrains Marketplace and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

- Manually:

  Download the [latest release](https://github.com/bsafwen/sbt-codeartifact-creds/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


---
