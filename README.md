# Ballerina plugin for IntelliJ IDEA

## Building from the source

1. Clone the repository using the following command.
```
git clone --recursive https://github.com/ballerinalang/plugin-intellij.git
```
2. Make sure that the bundled **Plugin DevKit** plugin is enabled in IDEA.
3. Open the project in IDEA.
4. Configure a common JDK and an IntelliJ Platform SDK. 

   This can be done in **File -> Project Structure -> SDKs**.

   For the IntelliJ Platform SDK, you may need to specify the directory containing the installed version of IntelliJ IDEA. By default it should offer the directory of the currently running version of IntelliJ IDEA.
5. Build the project using **Build -> Build Project**.
6. Run **Build -> Prepare Plugin Module 'Ballerina-intellij-plugin' For Deployment**. 
7. In the project root directory, **Ballerina-intellij-plugin.zip** will be created.

## Installing the plugin to IDEA
1. Go to **File -> Settings** and select **Plugins**.
2. Click **Install plugin from disc** button and select the deployed **plugin zip** file. Please make sure to install the Zip file, not the extracted Jar files. This zip contains an additional library as well. Without this library, the plugin will not work properly.
3. Restart IDEA.

## Running the plugin without building
1. Follow the first 4 steps in the [Building from the source](#building-from-the-source) section.
2. Go to **Run -> Edit Configurations**.
3. Add a new **plugin** configuration.
4. Now you can run or debug the plugin directly from the IDEA.

## Getting started

Please refer the [Getting Started](getting-started) section.

## How to contribute
Pull requests are highly encouraged and we recommend you to create a GitHub issue to discuss the issue or feature that you are contributing to.

## License
Ballerina IDEA plugin source is available under the Apache 2.0 License.

## Copyright
Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
