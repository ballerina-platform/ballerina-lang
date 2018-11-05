# Plugin Developer Guide

## Testing the plugin using IDEA

1. Go to **File -> Open** and open the cloned repository using IntelliJ IDEA.

![alt text](images/Figure-1.png)

2. **Import Project from Gradle** settings window will be shown. Select the **Gradle Home** path and select **OK**.

![alt text](images/Figure-2.png)

3. From the **Gradle projects** tool window, run `runIde` task. This will build the plugin and a new IDEA instance will be started with the plugin installed.

If the **Gradle projects** window is not visible, you can use **View -> Tool Windows -> Gradle** to go to the Gradle projects tool window.

![alt text](images/Figure-3.png)

4. In addition to the above method, you can also add a **Gradle configuration** to **Run** or **Debug** the plugin.

* Go to **Run -> Edit Configurations**.

* Add a new Gradle Configuration.

![alt text](images/Figure-4.png)

* Select the **plugin-intellij** project as the **Gradle project**.

![alt text](images/Figure-5.png)

* Add `runIde` to the **Tasks**.

![alt text](images/Figure-6.png)

* Now you can **Run** or **Debug** the plugin using the created Gradle configuration very easily.

![alt text](images/Figure-7.png)
