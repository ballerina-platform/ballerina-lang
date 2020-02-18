# Contributing to Ballerina

Ballerina is an open source programming language and platform for cloud-era application programmers to easily write software. It is licensed under the [Apache license](https://www.apache.org/licenses/LICENSE-2.0) and is nurtured by all the contributors of the Ballerina community.

We appreciate your help!

- [Get started](#get-started)
- [Set up the Prerequisites](#set-up-the-prerequisites)
- [Build the source code](#build-the-source-code)
- [Set up the development environment](#set-up-the-development-environment)
    - [Set up the Ballerina plugins](#set-up-the-ballerina-plugins)
    - [Set up the ANTLR4 plugin](#set-up-the-antlr4-plugin)
- [Submit your contribution](#submit-your-contribution)
- [Accept the Contributor License Agreement](#accept-the-contributor-license-agreement)
- [Propose changes to Ballerina](#propose-changes-to-ballerina)

## Get started

- Join the conversations at:

    - [StackOverflow](https://stackoverflow.com/questions/tagged/ballerina): to get help with Ballerina (use the Ballerina tag for any of your questions)
    - [Slack](https://ballerina.io/community/slack/): for real-time discussions with the team and community
    - [Ballerina-Dev Google Group](https://groups.google.com/forum/#!forum/ballerina-dev): (community mailing list) to discuss Ballerina roadmap, features, and related issues
    - [Twitter](https://twitter.com/ballerinalang): to tweet about Ballerina (use the “#ballerinalang” hashtag) 

- Submitting a bug is just as important as contributing to code. Report an issue in the relevant repo out of the GitHub repos listed below. 

    >**Tip:** If you are unsure whether you have found a bug, search existing issues in the corresponding repo on GitHub and raise it in the [Ballerina-Dev Google Group](#https://groups.google.com/forum/#!forum/ballerina-dev).
    - Compiler, runtime, standard library, or tooling: <a href="https://github.com/ballerina-platform/ballerina-lang/issues">ballerina-lang</a> repo
    - Language specification: <a href="https://github.com/ballerina-platform/ballerina-spec/issues">ballerina-spec</a> repo
    - Website: <a href="https://github.com/ballerina-platform/ballerina-dev-website/issues">ballerina-dev-website</a> repo
    - Security flaw: send an email to security@ballerina.io. For details, see the <a href="https://ballerina.io/security/">security policy</a>.

-  Start with GitHub issues that can be fixed easily:
    - Browse issues labeled "good first issue" in the <a href="https://github.com/ballerina-platform/ballerina-lang/issues">ballerina-lang</a> repo.
    - Use comments on the issue itself to indicate that you will be working on it and get guidance and help.

## Set up the prerequisites
1. Download [Ballerina](https://ballerina.io) and go through the [learning resources](https://ballerina.io/learn).
2. Read the <a href="https://ballerina.io/code-of-conduct">Ballerina Code of Conduct</a>.

## Build the source code 

For instructions, see <a href="https://ballerina.io/learn/installing-ballerina/#installing-from-source">Installing from source</a>.

## Set up the development environment

See the below sections to set up the required plugins in your preferred IDEs/Editors.

### Set up the Ballerina plugins

Currently, Ballerina has the below plugins developed for IntelliJ IDEA and VS Code. 
- [Ballerina plugin for IntelliJ IDEA](https://plugins.jetbrains.com/plugin/9520-ballerina)
    
    For installation instructions, see [Ballerina IntelliJ Plugin Documentation](https://ballerina.io/learn/intellij-plugin/).
- [Ballerina plugin for VS Code](https://marketplace.visualstudio.com/items?itemName=ballerina.ballerina)
    
    For installation instructions, see [Ballerina VS Code Documentation](https://ballerina.io/learn/vscode-plugin).

### Set up the ANTLR4 plugin

This plugin will be useful to check and validate a [grammar rule](#contributing-to-ballerina-grammar) you wrote. For instructions on installing the ANTLR4 plugin based on your preferred IDE/Editor (which supports ANTLR 4.x version), go to [ANTLR Documentation](https://www.antlr.org/tools.html).

## Submit your contribution

1. Do your changes in the source code.
    
    <details>
    <summary>If you are contributing to Ballerina Grammar:</summary>
    <div>

    <div style="border:1px solid #000; padding: 10px;">

    Ballerina grammar has been implemented using ANTLR plugin version 4.5.3. To get a basic understanding of ANTLR grammar syntax and concepts before working with Ballerina grammar, go to [Parr, Terence (January 15, 2013), The Definitive ANTLR 4 Reference](https://www.oreilly.com/library/view/the-definitive-antlr/9781941222621/).

    [Ballerina grammar](https://github.com/ballerina-platform/ballerina-lang/tree/master/compiler/ballerina-lang/src/main/resources/grammar) consists of two files:

    - **BallerinaLexer.g4:** contains the lexer rules for Ballerina grammar. The lexer is responsible for tokenizing an input Ballerina source code.
    - **BallerinaParser.g4:** contains the parser rules. Parser listens to the token stream generated by the lexer. High level grammar productions/abstractions are defined in the parser using those tokens.

    Once a change is done to any of the grammar files, the lexer and the parser need to be re-generated. To generate the lexer and the parser, navigate to the `<BALLERINA_PROJECT_ROOT>/compiler/ballerina-lang/src/main/resources/grammar/` directory, and execute the below command. 

    >**Tip:** Download the [antlr-complete-4.5.3.jar](https://jar-download.com/artifacts/org.antlr/antlr4/4.5.3/source-code) file and replace `<PATH-TO-ANTLR-JAR>` in the below command with the location in which you saved it.

    ```bash 
    java -jar <PATH-TO-ANTLR-JAR>/antlr-4.5.3-complete.jar *.g4 -package org.wso2.ballerinalang.compiler.parser.antlr4 -o <BALLERINA_PROJECT_ROOT>/compiler/ballerina-lang/src/main/java/org/wso2/ballerinalang/compiler/parser/antlr4/
    ```

    **Info:** The above command will autogenerate some Java classes. The Ballerina AST builder is written on top of the auto-generated `BallerinaParserBaseListener.java` class. Thus, if any new rules are added to the `BallerinaParser.g4`, above command will generate new methods in the `BallerinaParserBaseListener.java` and you need to override those newly-added methods inside the `BLangParserListener.java` accordingly.

    For more information about the Ballerina compiler, go to [Ballerina Compiler — Design](https://medium.com/@sameerajayasoma/ballerina-compiler-design-3406acc2476c?).

    </div>
    </div>
    </details>

2. Add unit tests accordingly. (The build process will automatically execute all the tests.)
3. Commit the changes to your fork and push them to the corresponding original repo by sending a Pull Request (PR). 

    >**Tip:** Follow these commit message requirements:

    - Separate subject from the body with a blank line
    - Limit the subject line to 50 characters
    - Capitalize the subject line
    - Do not end the subject line with a period
    - Use the imperative mood in the subject line
    - Wrap the body at 72 characters
    - Use the body to explain what and why vs. how

For more information, go to [How to Write a Git Commit Message](https://chris.beams.io/posts/git-commit/).

## Accept the Contributor License Agreement 

You need to Accept the Ballerina Contributor License Agreement (CLA) here when prompted by GitHub while sending your first Pull Request (PR). Subsequent PRs will not require CLA acceptance.

If the CLA gets changed for some (unlikely) reason, you will be presented with the new CLA text when sending your first PR after the change.

## Propose changes to Ballerina

Start the discussion on the changes you propose in the [Ballerina-Dev Google Group](https://groups.google.com/forum/#!forum/ballerina-dev). Once there is enough consensus around the proposal, you will likely be asked to file an issue on GitHub and label it as 'Proposal' to continue a detailed discussion there.
