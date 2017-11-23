# Compile Ballerina Programs

You can execute Ballerina programs (`.bal` files) directly from the command line. However, if you want to create a self-contained executable program containing all the program code and third-party dependencies, you must build the program into a compiled `.balx` file. For example, if you want to deploy your Ballerina service to [WSO2 Integration Cloud](http://wso2.com/integration/cloud/) so that others can access that service in the cloud, you create a `.balx` file of your Ballerina program and upload it to the cloud. This tutorial shows you how to create an archive of the HelloWorldService sample. 

## Declare the package name
Before you can compile your Ballerina program, you must declare a package name. This package name provides a unique identifier for your program. The path in the package name must match the directory structure where the Ballerina file is located, starting from the top directory where your programs are located. For example, if you want to create compiled Ballerina sample programs, you would declare the package names in each of the samples as `samples.<sampleName>`, such as `samples.helloWorldService`. When you compile your Ballerina program, you will use the package name with the `ballerina build` command to specify which program you want to archive.

You declare the package name at the very top of the Ballerina program file. Let's declare a package name for the HelloWorldService sample.

1. In your `<ballerina_home>/samples/helloWorldService` directory, open the `helloWorldService.bal` file for editing. You can use the Composer or any text editor.
1. At the top of the file, add the following line to declare the package name:

```Ballerina
package samples.helloWorldService;
```

The sample should now look like this:

```Ballerina
package samples.helloWorldService;
import ballerina.net.http;

@http:configuration {basePath:"/hello"}
service<http> helloWorld {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource sayHello (http:Request req, http:Response resp) {
        resp.setStringPayload("Hello, World!");
        resp.send();
    }
}
```
Now that you have declared the package name for the HelloWorldService program, you can compile the program by specifying its package name.

## Compile the Ballerina program
To compile the Ballerina program, you will use the `ballerina build` command. Specify the file or package to compile, and optionally specify the output name to give the compiled program file. When you specify the package name in this command, note that you will use slashes instead of periods to delineate the path, such as `samples/helloWorldService` instead of `samples.helloWorldService`. If you don't specify an output name for the compiled program, the last part of the package name is used as the name, e.g., `helloWorldService.balx`. In this tutorial, we will name the compiled program file `hello.balx`.

Let's compile the Ballerina program.

1. Navigate to your `<ballerina_home>` directory.
1. If your `<ballerina_home>/bin` directory is already in your path, type the `ballerina` command as shown below. If it is not in your path, type `bin/ballerina` (or `bin\ballerina` on Windows) instead of `ballerina`. 
 
  ```bash
  ballerina build service samples/helloWorldService -o hello.balx
  ```

You have now compiled your Ballerina program, which you can upload to WSO2 Integration Cloud, run in a Docker container, or distribute to other Ballerina users.
