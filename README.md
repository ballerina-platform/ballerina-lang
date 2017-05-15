# Ballerina plugin for IntelliJ IDEA

## Building from the source

1. Clone the repository using the following command.

    ```
    git clone --recursive https://github.com/ballerinalang/plugin-intellij.git
    ```
2. Install latest version of [Gradle](https://gradle.org/) if you don't have it installed already.
3. Navigate into the cloned repository and run `gradle buildPlugin`. In the **build/distributions** directory, **Ballerina-Intellij-Plugin.zip** will be created.

**Note:** Make sure to use ANTLR v4.6 to generate the Lexer and Parser if issues occur when using a version higher than v4.6. Also if you are using the ANTLR plugin for Intellij to generate the ANTLR Recognizer, use plugin v1.8.3 which includes ANTLR v4.6.

## Installing the plugin to IDEA
1. Go to **File -> Settings** (**IntelliJ IDEA -> Preferences** in **macOS**) and select **Plugins**.
2. Click **Install plugin from disc** button and select the deployed **plugin zip** file. Please make sure to install the Zip file, not the extracted Jar files. This zip contains an additional library as well. Without this library, the plugin will not work properly.
3. Restart IDEA.

## Release versions schema

Below you can see the versions of the plugin which correspond to the versions of the 
IntelliJ Platfom.

| Plugin Version | Platform Version |
| --- | --- |
| 0.8.0 - 0.8.2 | IntelliJ IDEA 2016.3 - 2016.* |
| 0.8.3+ | IntelliJ IDEA 2016.3+ |

## Getting started

Please refer the [Getting Started](getting-started) section.

## How to contribute
Pull requests are highly encouraged and we recommend you to create a GitHub issue to discuss the issue or feature that you are contributing to.

## License
Ballerina IDEA plugin source is available under the Apache 2.0 License.

## Copyright
Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
