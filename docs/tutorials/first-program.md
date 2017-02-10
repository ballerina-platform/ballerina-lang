# Writing your First Program

Now that you’ve taken the [Quick Tour](../quick-tour.md), let's dig a little deeper and take the leap into writing your first Ballerina integration program. This tutorial will teach you how to run Ballerina in standalone and server mode, use the editor to build your integration, understand the key concepts, and get familiar with the Ballerina language.

## Running Ballerina

In the [Quick Tour](../quick-tour.md), you learned how to start Ballerina and run a sample program with a single command:

```
./ballerina run helloworld.bal
```

After the HelloWorld program executed, Ballerina stopped. This approach is called **standalone mode**, and it's useful when you want to execute a program once and then stop as soon as it has finished its job. It runs the `main()` function of the program you specify and then exits. 

You can also run Ballerina as a **server**, so that it can deploy a program as a service that waits for requests. To see how this works, let's go to the Ballerina `bin` directory, and then run Ballerina in server mode and deploy the HelloWorldService program:

```
./ballerina service ../samples/helloWorldService/helloWorldService.bal
```

In this case, Ballerina ran and deployed the HelloWorldService program as a service, which is waiting for a request. Let's send it one now. The Ballerina server is available at `localhost:9090`, and HelloWorldService is available at context `hello`. Open another command line and use the [curl](https://curl.haxx.se) client to call the service as follows:

```
curl -v http://localhost:9090/hello
```

The service receives the request and executes its logic, printing "Hello, World" on the command line. Notice that the Ballerina server is still running in the background, waiting for more requests to come in.

## Using the Editor

Now that you understand how to run Ballerina in standalone and server mode, let's play with the editor. Run it from the command line like this (omit the `sh` prefix if you're running on Windows):

```
sh editor
```

And now access it from the following URL in your browser: http://localhost:9091

The Ballerina editor appears. Notice that on the left you have a tool palette of the various constructs that you'll use to build your integration. On the right, you have a visual editor with a canvas onto which you drag those constructs. This is where you'll build your sequence diagrams that define your integration logic. 

Additionally, look for the source code icon in the lower right corner (`</>`). Click it now, and you'll see the source code that represents the sequence diagram as code in the Ballerina language. In a new file with no content, the only source code will be a line importing the Ballerina language package. Click the icon again to return to the visual editor. You can go back and forth between the visual editor and the source code and make your edits in either place.

## Building an integration

It's time to build your first integration! In this exercise, we are going to build a service that takes an incoming message, extracts the text, and sends a message back to the client with that same text. 

### Add a service and resource

First, we add a service construct to the canvas. A **service** is a container for all the other constructs and represents a single unit of functionality that can be accessed remotely. Technically, it's an HTTP web service described by a Swagger definition file. 

1. On the tool palette, click and drag the service icon to the canvas. 

A box appears with the name `newService`, and inside it is another box called `newResource` with some logic already created for you. 

A **resource** is a single request handler within a service. This is where we will program the logic describing how to handle the incoming message to this service. By default, the resource is configured to accept a message `m`. You can see this by clicking the Parameters icon in the upper right corner of the resource box. When you click it, you'll see `message m` listed below the fields. Click the Parameters icon again to close its window.

Let's rename both the service and resource. 

1. Highlight the name `newService` and type `echo` in its place. 
1. Change `newResource` to `echoResource` in the same way. 

### Set the base path

Now, let's set the base path for this service. This will be the context portion of the URL that is used to send requests to this service. 

1. In the upper right corner of the service box (not the resource box this time), click the `@` symbol. 
1. Make sure BasePath is selected in the list, type `/echo2` in the text box, and then press Enter or click the + symbol to its right. 

   We are naming it `/echo2` so that it doesn't conflict with the existing echo sample. 

The base path is now set, so that when you deploy this service, clients will be able to send requests to it using the URL http://localhost:9090/echo2.

### Change GET to POST

When you added the service, Ballerina configured the resource to use the GET method by default. Because we are going to use the incoming message to post a reply, let's change it to POST. 

1. Click the `@` symbol in the upper right corner of the resource box, highlight `GET`, and type `POST`. 
1. Click the `@` symbol again to hide the box. 

You can click the symbol again to confirm that GET was in fact changed to POST. You can also click the source (`</>`) icon in the lower right corner to see the changes that are being made to the code as you work with the visual editor.

### Add a function

Now we are ready to add a function that will take the incoming message and convert it to a response that gets sent back to the client. The ballerina.net.http package includes a native function called `convertToResponse` that does exactly this, so let's import that package and add the function to our flow.

1. In the upper right corner of the canvas, click the package icon, type `ballerina.net.http`, and press Enter. 
...You have now imported the package containing the native function we want to use. 
1. In the Statements section of the tool palette, grab the Function Invocation icon and drag it under the start box in the sequence diagram. 
1. Click the function box you just added and click the Edit icon (it looks like a pencil). 
1. For the package name, type `http`. 
1. For the FunctionName, type `convertToResponse`. 
1. For Params, type `m`, which is the incoming message. 

### Add the reply
Now that we've added the function that will convert the incoming message text to a response, we just need to instruct the program to send the response back to the client.

1. On the tool palette, grab the Reply icon (the straight left arrow) and drag it to the canvas under the function invocation you just added. You'll see that it appears as a box with an arrow going back to the client. 
1. Click the box, click the Edit icon, and set the response message to m, which instructs the program to send the message processed by the convertToResponse function back to the client. 

This completes the sequence, so you are now ready to save and run your integration program.

### Save the program

1. Click the **File** menu and choose **Save As**. 
1. Save it as `myEcho.bal` in your Ballerina `bin` directory. 

After you save the program, a file explorer on the left shows the `bin` directory. You can expand it to see that the file is there, and then click the file explorer icon to close the file explorer. The file explorer is a handy way to open files inside the editor in the future.

### Run the program

1. At your command prompt, navigate to your Ballerina `bin` directory, and enter the command to run the Ballerina server and deploy your myEcho program:

  ```
  ./ballerina service myEcho.bal
  ```

Your service is now deployed and running on the Ballerina server. 

### Send the request

1. From a separate command prompt, use curl to send a request to your program:

  ```
  curl -v http://localhost:9090/echo2 -d "Hello World......"
  ```

The service receives the request, takes the text `Hello World......` from the incoming message, converts it into a response, and sends it back to the command line where the request was sent.

You have now completed the tutorial! If you run into problems, you can troubleshoot by comparing your code with the Echo sample.  

## Next steps

Now that you're familiar with running Ballerina in standalone and server mode, using the editor to build an integration program, and creating a service and resource, you are ready to learn more. 

* Read the [Key Concepts](../key-concepts.md) page to familiarize yourself with the rest of the main features you need to know about.
* Browse through the [Samples](../samples/index.md) and use the relevant ones as templates for your own programs.
* See the [Language Reference](../lang-ref/index.md) for complete information on using the Ballerina language. 
