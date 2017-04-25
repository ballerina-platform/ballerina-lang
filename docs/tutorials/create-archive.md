# Create an Archive of your Program

You can execute Ballerina programs (`.bal` files) directly from the command line. However, if you want to create a self-contained package containing all the program code and third-party dependencies, you must build the program into a packaged format: a service archive (`.bsz`) or executable archive (`.bmz`). For example, if you want to deploy your Ballerina service to [WSO2 Integration Cloud](http://wso2.com/integration/cloud/), you must upload a service archive of your Ballerina program. This tutorial shows you how to create an archive of the HelloWorldService sample. 

## Declaring the package name
Before you can create your archive, you must declare a package name for your Ballerina program. You will use this package name with the `ballerina build` command to specify the program you want to archive. The package path must match the directory structure where the file is located, starting from the top directory where your programs are located. For example, if you want to create archives of the Ballerina sample programs, you would declare the package names in each of the samples as `samples.<sampleName>`, such as `samples.helloWorldService`. 

You add the package declaration at the very top of the Ballerina program file. Let's declare a package name for the HelloWorldService sample.

1. In your `<ballerina_home>/samples/helloWorldService` directory, open the `helloWorldService.bal` file for editing. You can use the Composer or any text editor.
1. At the top of the file, add the following line to declare the package name:

```
package samples.helloWorldService;
```

The sample should now look like this:

```
package samples.helloWorldService;
import ballerina.lang.messages;
@http:BasePath ("/hello")
service helloWorld {
    
    @http:GET
    resource sayHello (message m) {
        message response = {};
        messages:setStringPayload(response, "Hello, World!");
        reply response;
    
    }
    
}
```
Now that you have declared the package name for the program, you can build the archive.

## Creating the archive
To create the archive, you will use the `ballerina build` command followed by the archive type (`service` or `main`), specify one or more packages to include in the archive, and optionally specify the output name to give the archive file. When you specify the package name in this command, note that you will use slashes instead of periods to delineate the path, such as `samples/helloWorldService` instead of `samples.helloWorldService`. If you don't specify an output name for the archive file, the last part of the package name is used as the name, e.g., helloWorldService.bsz. In this tutorial, we will name it `hello.bsz`.

Let's create the archive.

1. Navigate to your `<ballerina_home>` directory.
1. If your `<ballerina_home>/bin` directory is already in your path, type the `ballerina` command as shown below. If it's not in your path, type `bin/ballerina` (or `bin\ballerina` on Windows) instead of `ballerina`. 
  ```
  ballerina build service samples/helloWorldService -o hello.bsz
  ```

You now have a service archive file of your Ballerina program, which you can upload to WSO2 Integration Cloud, run in a Docker container, or easily distribute to others. For more information on packages, archives, and running Ballerina programs, see [Packaging and Running Programs](../lang-ref/packaging.md). 
