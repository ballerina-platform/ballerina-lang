## Running the plugin for development

1. Install [maven](https://maven.apache.org/install.html) and [nodejs](https://nodejs.org/en/)
2. Clone this repo
3. Run `npm install` and `mvn clean install`
4. Run vscode with `code --extensionDevelopmentPath=/path/to/ballerina/plugin-vscode`

## Debugging language server

To connect a remote debug client to the language server process, set `LSDEBUG` environment variable to "true".

`LSDEBUG=true code --extensionDevelopmentPath=/home/aruna/projects/ballerina/plugin-vscode`

Now connect the remote debug client to port 5005.