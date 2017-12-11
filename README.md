# Ballerina plugin for Visual Studio Code

## What is Ballerina

ballerina is a general purpose, concurrent and strongly typed
programming language with both textual and graphical syntaxes.

for more info: http://ballerinalang.org/

## Installing the plugin to Visual Studio Code(vscode)

1. Install [maven](https://maven.apache.org/install.html) and [nodejs](https://nodejs.org/en/)
2. Clone this repo
3. Run `npm install` and `mvn clean install`
4. Run vscode with `code --extensionDevelopmentPath=/path/to/ballerina/plugin-vscode`

Alternatively, in your home directory, locate .vscode/extentions/ directory and add the root directory into it.

## Debugging language server

To connect a remote debug client to the language server process, set `LSDEBUG` environment variable to "true".

`LSDEBUG=true code --extensionDevelopmentPath=/home/aruna/projects/ballerina/plugin-vscode`

Now connect the remote debug client to port 5005.

## How to contribute

Pull requests are highly encouraged and we recommend you to create a GitHub issue
to discuss the issue or feature that you are contributing to.

## License

Ballerina Visual Studio Code plugin source is available under the Apache 2.0 License.

## Copyright

Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
