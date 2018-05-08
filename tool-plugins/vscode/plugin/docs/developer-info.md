## Running the plugin for development

1. Install [maven](https://maven.apache.org/install.html) and [nodejs](https://nodejs.org/en/)
2. Clone this repo
3. Run `mvn clean install` from the root of the project
4. Run vscode with `code --extensionDevelopmentPath=/path/to/ballerina/tool-plugins/vscode/plugin`

## Installing from the `vsix` file

The plugin is packaged into a file named `ballerina-vscode-plugin-[VERSION].vsix` created in the directory `ballerina/tool-plugins/vscode/plugin/target`.

To install the package using it, run:

`code --install-extension ballerina-vscode-plugin-[VERSION].vsix`

## Debugging language server

To make sure that the plugin is using the most recent language server core set `ballerina.classpath` configuration of the plugin to point to the language server core jar.

~~~
ballerina.classpath : "/path/to/ballerina/language-server/modules/langserver-core/target/*"
~~~

This can be used to update the classpath of java services used by the plugin in any other way.

To connect a remote debug client to the language server process, set `LSDEBUG` environment variable to "true".

`LSDEBUG=true code --extensionDevelopmentPath=/path/to/ballerina/tool-plugins/vscode/plugin`

Now connect the remote debug client to port 5005.