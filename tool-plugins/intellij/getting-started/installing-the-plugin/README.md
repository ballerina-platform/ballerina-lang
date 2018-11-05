## Installing the plugin in IDEA

### From Jetbrains plugin repository
1. Go to **Settings** (**Preferences** in **MacOS**)-> **Plugins**. 
2. Select **Browse Repositories** button at the bottom. 
3. Search for **Ballerina** using the search box. Ballerina plugin will show up. Then you can install the plugin using the Install button.
4. Restart IDEA.

### From a local build

1. Run `./gradlew buildPlugin`. In the **build/distributions** directory, **ballerina-intellij-idea-plugin-[VERSION].zip** will be created.
>**Note:** On Windows, you don’t need the leading `./` in front of the `gradlew` command

2. Go to **File -> Settings** (**IntelliJ IDEA -> Preferences** in **macOS**) and select **Plugins**.
3. Click **Install plugin from disc** button and select the deployed **plugin zip** file. Please make sure to install the Zip file, not the extracted Jar files. This zip contains an additional library as well. Without this library, the plugin will not work properly.
4. Restart IDEA.