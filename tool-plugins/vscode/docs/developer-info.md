## Running the plugin for development

1. Install [maven](https://maven.apache.org/install.html) and [nodejs](https://nodejs.org/en/)
2. Clone this repo
3. Run `mvn clean install` from the root of the project
4. Run vscode with `code --extensionDevelopmentPath=/path/to/ballerina/tool-plugins/vscode`

## Installing from the `vsix` file

The plugin is packaged into a file named `ballerina-vscode-plugin-[VERSION].vsix` created in the directory `ballerina/tool-plugins/vscode/target`.

To install the package using it, run:

`code --install-extension ballerina-vscode-plugin-[VERSION].vsix`

## Debugging language server

To connect a remote debug client to the language server process, set `LSDEBUG` environment variable to "true".

`LSDEBUG=true code --extensionDevelopmentPath=/home/aruna/projects/ballerina/plugin-vscode`

Now connect the remote debug client to port 5005.