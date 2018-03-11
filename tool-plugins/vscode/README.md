# Ballerina plugin for Visual Studio Code

## How to install

Find the plugin in the visual studio [marketplace](https://marketplace.visualstudio.com/items?itemName=WSO2.Ballerina) and follow the instructions.

### Configuring the Ballerina SDK path

If you plan on using ballerina libraries other than the ones in the standard ballerina distribution you should set the `ballerina.sdk` setting to the path to the directory of extracted ballerina distribution.

To get to the settings in vscode use <kbd>CTRL</kbd> + <kbd>,</kbd> in Windows and Linux and <kbd>⌘</kbd> + <kbd>,</kbd> in macOS. [More info](https://code.visualstudio.com/docs/getstarted/settings).

![set sdk](https://github.com/ballerina-lang/ballerina/blob/master/tool-plugins/vscode/docs/set-sdk.gif?raw=true)

## Usage

### Editor features

* Code completion : <kbd>CTRL</kbd> + <kbd>SPACE</kbd>
* Go to definition 
    * Jump to source: <kbd>CTRL</kbd> + <kbd>Click</kbd>
    * Open to the side with <kbd>CTRL</kbd> + <kbd>ALT</kbd> + <kbd>Click</kbd>
* Go to symbol : <kbd>CTRL</kbd> + <kbd>SHIFT</kbd> + <kbd>O</kbd>

### Debugger

#### Configuring the debugger

* Open a `.bal` file
* Go to the debug panel (Ctrl + Shift + D)
* Click on 'Configure or fix launch.json'

This will create a `.vscode/launch.json` file in your workspace, containing default ballerina debug configurations.

![opening debug config](https://github.com/ballerina-lang/ballerina/blob/master/tool-plugins/vscode/docs/debugger-open-config.gif?raw=true)

There are two configurations with type `"ballerina"`. One with `"request"` set to `"launch"` and the other to `"attach"`. The second is used for remote debugging.

To launch the debugger, the path to the ballerina sdk needs to be configured. In the `"launch"` configurations add a new config named `"ballerina.sdk"` and provide the path to the sdk.

![setting ballerina sdk](https://github.com/ballerina-lang/ballerina/blob/master/tool-plugins/vscode/docs/debugger-ballerina-sdk.gif?raw=true)

#### Using the debugger

* Add necessary debug points.
* Select 'Ballerina Debug' from the config menu. And do Debug -> Start Debugging.

![using debugger](https://github.com/ballerina-lang/ballerina/blob/master/tool-plugins/vscode/docs/debugger-using.gif?raw=true)

## Contributing

If you want to help develop the ballerina vscode plugin or want to build from source and try out the latest features see [information for developers](https://github.com/ballerina-lang/ballerina/blob/master/tool-plugins/vscode/docs/developer-info.md).

## About Ballerina

ballerina is a general purpose, concurrent and strongly typed programming language with both textual and graphical syntaxes, optimized for integration.

for more info: http://ballerinalang.org/

## License

Ballerina Visual Studio Code plugin source is available under the Apache 2.0 License.

## Copyright

Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
