# Ballerina plugin for IntelliJ IDEA

[![Version](https://img.shields.io/jetbrains/plugin/v/9520-ballerina.svg)](https://plugins.jetbrains.com/plugin/9520-ballerina)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/9520-ballerina.svg)](https://plugins.jetbrains.com/plugin/9520-ballerina)

## Building from the source

Run `./gradlew buildPlugin`. In the **build/distributions** directory, **ballerina-intellij-idea-plugin-[VERSION].zip** will be created.

**Note:** On Windows, you don’t need the leading `./` in front of the `gradlew` command

## Installing the plugin to IDEA

### From Jetbrains plugin repository
1. Go to **Settings** (**Preferences** in **MacOS**)-> **Plugins**. 
2. Select **Browse Repositories** button at the bottom. 
3. Search for **Ballerina** using the search box. Ballerina plugin will show up. Then you can install the plugin using the Install button.
4. Restart IDEA.

**Note:** Refer the [Getting Started](https://github.com/ballerinalang/plugin-intellij#getting-started) section to view details about setting up Ballerina SDK.

### From a local build
1. Go to **File -> Settings** (**IntelliJ IDEA -> Preferences** in **macOS**) and select **Plugins**.
2. Click **Install plugin from disc** button and select the deployed **plugin zip** file. Please make sure to install the Zip file, not the extracted Jar files. This zip contains an additional library as well. Without this library, the plugin will not work properly.
3. Restart IDEA.

## Release versions schema

Below you can see the versions of the plugin which correspond to the versions of the 
IntelliJ Platfom.

| Plugin Version | Platform Version |
| --- | --- |
| 0.8.0 - 0.8.2 | IntelliJ IDEA 2016.3+ (Does not support IntelliJ IDEA 2017+)|
| 0.8.3+ | IntelliJ IDEA 2016.3+ (Supports IntelliJ IDEA 2017+)|

## Getting started

Please refer the [Getting Started](getting-started) section.

## Semantic Analyzing

Ballerina IDEA plugin provides semantic analyzing capabilities through Ballerina SDK. But this feature is disabled by default due to performance related reasons.

You can enable this in `Settings -> Languages & Frameworks -> Ballerina -> Semantic Analyzer`.

![alt text](getting-started/features/images/SemanticAnalyzing.png)

## How to contribute
Pull requests are highly encouraged and we recommend you to create a GitHub issue to discuss the issue or feature that you are contributing to.

## License
Ballerina IDEA plugin source is available under the Apache 2.0 License.

## Copyright
Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
