# Contributing to Ballerina

Ballerina is an open source programming language and platform for cloud-era application programmers to easily write software. It is licensed under the [Apache License](https://www.apache.org/licenses/LICENSE-2.0) and is nurtured by all the contributors of the Ballerina community.

We appreciate your help!

- [Get started](#get-started)
- [Build the source code](#build-the-source-code)
- [Set up the development environment](#set-up-the-development-environment)
    - [Set up the Ballerina plugins](#set-up-the-ballerina-plugins)
    - [Set up the ANTLR4 plugin](#set-up-the-antlr4-plugin)
- [Submit your contribution](#submit-your-contribution)
- [Accept the Contributor License Agreement](#accept-the-contributor-license-agreement)
- [Propose changes to Ballerina](#propose-changes-to-ballerina)

## Get started

- Download [Ballerina](https://ballerina.io/downloads/) and go through the [learning resources](https://ballerina.io/swan-lake/learn).
- Read the <a href="https://ballerina.io/code-of-conduct">Ballerina Code of Conduct</a>.

- Join the conversations at:

    - [StackOverflow](https://stackoverflow.com/questions/tagged/ballerina): to get help with Ballerina (use the Ballerina tag for any of your questions)
    - [Slack](https://ballerina.io/community/slack/): for real-time discussions with the team and community
    - [Twitter](https://twitter.com/ballerinalang): to tweet about Ballerina (use the “#ballerinalang” hashtag) 
    - [GitHub](https://github.com/ballerina-platform/ballerina-lang/issues): to file issues, join the conversations on new features, comment on other issues, and send your pull requests.

- Submitting a bug is just as important as contributing to code. Report an issue in the relevant repo out of the GitHub repos listed below. 

    >**Tip:** If you are unsure whether you have found a bug, search existing issues in the corresponding repo on GitHub and raise it in the [Ballerina Slack channel](#https://ballerina-platform.slack.com/).
    - Compiler, runtime, standard library, or tooling: <a href="https://github.com/ballerina-platform/ballerina-lang/issues">ballerina-lang</a> repo
    - Language specification: <a href="https://github.com/ballerina-platform/ballerina-spec/issues">ballerina-spec</a> repo
    - Website: <a href="https://github.com/ballerina-platform/ballerina-dev-website/issues">ballerina-dev-website</a> repo
    - Security flaw: send an email to security@ballerina.io. For details, see the <a href="https://ballerina.io/security/">security policy</a>.

-  Start with GitHub issues that can be fixed easily:
    - Browse issues labeled "good first issue" in the <a href="https://github.com/ballerina-platform/ballerina-lang/issues">ballerina-lang</a> repo.
    - Use comments on the issue itself to indicate that you will be working on it and get guidance and help.

## Build the source code 

For instructions, see <a href="https://ballerina.io/learn/installing-ballerina/building-from-source/">Building from source</a>.

## Set up the development environment

See the below sections to set up the required plugins in your preferred IDEs/Editors.

### Set up the Ballerina plugins

Set up the [Ballerina plugin for VS Code](https://marketplace.visualstudio.com/items?itemName=ballerina.ballerina)
    
For installation instructions, see [Ballerina VS Code Documentation](https://ballerina.io/swan-lake/learn/getting-started/setting-up-visual-studio-code/).

## Submit your contribution

1. Do your changes in the source code.
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
4. Accept the Contributor License Agreement (CLA)
    
    You need to Accept the Ballerina Contributor License Agreement (CLA) when prompted by a GitHub email notification after sending your first Pull Request (PR). Subsequent PRs will not require CLA acceptance.

    If the CLA gets changed for some (unlikely) reason, you will be presented with the new CLA text after sending your first PR after the change.

## Propose changes to Ballerina

Start the discussion on the changes you propose in the [Ballerina Slack channel](https://ballerina-platform.slack.com/). Once there is enough consensus around the proposal, you will likely be asked to file an issue on GitHub and label it as 'Proposal' to continue a detailed discussion there.
