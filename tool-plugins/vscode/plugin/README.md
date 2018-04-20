# Ballerina plugin for Visual Studio Code

[Ballerina](http://ballerina.io) makes it easy to build resilient services that integration and orchestrate across distributed endpoints. This plugin adds language support for Ballerina to Visual Studio Code.

## Quick start
- Download and install ballerina platform from [ballerina.io](https://ballerina.io/downloads/).
- Install VSCode plugin.
    - **Option 1.** Install this extension from the VSCode [marketplace](https://marketplace.visualstudio.com/items?itemName=WSO2.Ballerina) (or by entering `ext install WSO2.Ballerina` at the command palette).
    - **Option 2.** Download and install the VSCode plugin manually.
        - Download `ballerina-vscode-plugin-VERSION.vsix` from [ballerina.io](https://ballerina.io/downloads/). When downloading make sure plugin version matches the platform version you have installed.
        - Go to Install from VSIX... command in the Extensions View command drop-down, or the Extensions: Install from VSIX... command in the Command Palette, and select the .vsix file to install. [More info](https://code.visualstudio.com/docs/editor/extension-gallery#_install-from-a-vsix)
- Configure  `ballerina.sdk` path in settings. ( This step is mandatory for the Ballerina VSCode plugin features to work ) 
To get to the settings in vscode use <kbd>CTRL</kbd> + <kbd>,</kbd> in Windows and Linux and <kbd>⌘</kbd> + <kbd>,</kbd> in macOS. [More info](https://code.visualstudio.com/docs/getstarted/settings)

![set sdk](https://github.com/ballerina-platform/ballerina-lang/master/tool-plugins/vscode/plugin/docs/set-sdk.gif?raw=true)


## Plugin Features

### Language Support

* Code completion : <kbd>CTRL</kbd> + <kbd>SPACE</kbd>
* Go to definition 
    * Jump to source: <kbd>CTRL</kbd> + <kbd>Click</kbd>
    * Open to the side with <kbd>CTRL</kbd> + <kbd>ALT</kbd> + <kbd>Click</kbd>
* Go to symbol : <kbd>CTRL</kbd> + <kbd>SHIFT</kbd> + <kbd>O</kbd>
* Find All references
* Hover Support
* Signature Help
* Rename

### Diagram View

Ballerina’s underlying language semantics were designed by modeling how independent parties communicate via structured interactions. Subsequently, every Ballerina program can be displayed as a sequence diagram of its flow with endpoints, including synchronous and asynchronous calls. 

To view the sequence diagram of a ballerina file press the diagram icon ( ![design view icon](https://github.com/ballerina-platform/ballerina-lang/master/tool-plugins/vscode/plugin/docs/design-view-icon.png?raw=true) ) in top right corner.

![opening design view](https://github.com/ballerina-platform/ballerina-lang/master/tool-plugins/vscode/plugin/docs/ballerina-diagram-usage.gif?raw=true)

### Debugger

#### Configuring the debugger

* Open a `.bal` file
* Go to the debug panel (Ctrl + Shift + D)
* Click on 'Configure or fix launch.json'

This will create a `.vscode/launch.json` file in your workspace, containing default ballerina debug configurations.

![opening debug config](https://github.com/ballerina-platform/ballerina-lang/master/tool-plugins/vscode/plugin/docs/debugger-open-config.gif?raw=true)

There are two configurations with type `"ballerina"`. One with `"request"` set to `"launch"` and the other to `"attach"`. The second is used for remote debugging.

To launch the debugger, the path to the ballerina sdk needs to be configured. In the `"launch"` configurations add a new config named `"ballerina.sdk"` and provide the path to the sdk.

![setting ballerina sdk](https://github.com/ballerina-platform/ballerina-lang/master/tool-plugins/vscode/plugin/docs/debugger-ballerina-sdk.gif?raw=true)

#### Using the debugger

* Add necessary debug points.
* Select 'Ballerina Debug' from the config menu. And do Debug -> Start Debugging.

![using debugger](https://github.com/ballerina-platform/ballerina-lang/master/tool-plugins/vscode/plugin/docs/debugger-using.gif?raw=true)

## Contributing

If you want to help develop the ballerina vscode plugin or want to build from source and try out the latest features see [information for developers](https://github.com/ballerina-platform/ballerina-lang/master/tool-plugins/vscode/plugin/docs/developer-info.md).


## License

Ballerina Visual Studio Code plugin source is available under the Apache 2.0 License.

## Copyright

Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.

