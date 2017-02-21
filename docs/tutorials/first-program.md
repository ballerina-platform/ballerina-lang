# Write your First Program

Now that you’ve taken the [Quick Tour](../quick-tour.md), let's dig a little deeper and write your first Ballerina integration program. This tutorial will teach you how to run Ballerina in standalone and server mode, use the editor to build your integration, understand the key concepts, and get familiar with the Ballerina language.

## Running Ballerina

In the [Quick Tour](../quick-tour.md), you learned how to start Ballerina and run a sample program from the `<ballerina_home>/samples/helloWorld` directory with a single command:

```
ballerina run main helloWorld.bal
```

After the HelloWorld program executed, Ballerina stopped. This approach is called **standalone mode**, and it's useful when you want to execute a program once and then stop as soon as it has finished its job. It runs the `main()` function of the program you specify and then exits. 

You can also run Ballerina as a **server**, so that it can deploy one or more services that wait for requests. To see how this works, let's go to your `<ballerina_home>/samples/helloWorldService` directory, and then run Ballerina in server mode and deploy the HelloWorldService program:

```
ballerina run service helloWorldService.bal
```

In this case, Ballerina ran and deployed the HelloWorldService program as a service, which is waiting for a request. Let's send it one now. The Ballerina server is available at `localhost:9090`, and HelloWorldService is available at context `hello`. Open another command line window and use the [curl](https://curl.haxx.se) client to call the service as follows:

```
curl -v http://localhost:9090/hello
```

The service receives the request and executes its logic, printing "Hello, World!" on the command line. Notice that the Ballerina server is still running in the background, waiting for more requests to come in. You can stop the Ballerina server by pressing Ctrl-C. 

## Creating an integration

It's time to create your first integration! In this exercise, we are going to create a service that takes an incoming message, extracts the text, and sends a message back to the client with that same text. 

### Add a service and resource

First, we add a **service** construct to the canvas in the Composer. A service is a container for all the other constructs and represents a single unit of functionality that can be accessed remotely.

1. If the Composer is not already running, run it as described in the [Quick Tour](../quick-tour.md).

1. On the tool palette, click the service icon and drag it to the canvas. 

A box appears with the name `newService`, and inside it is another box called `newResource` with some content already created for you. 

A **resource** is a single request handler within a service. This is where we will program the logic describing how to handle certain types of requests from incoming messages to this service. 

By default, the resource is configured to accept a message `m`. You can see this by clicking the Arguments icon in the upper right corner of the resource box. When you click it, you'll see `message m` listed below the fields. Click the Arguments icon again to close its window.

Let's rename both the service and resource. 

1. Highlight the name `newService` and type `myEchoService` in its place. 
1. Change `newResource` to `myEchoResource` in the same way. 

### Set the base path

Now, let's set the base path for this service. This will be the context portion of the URL that clients will use to send requests to this service. 

1. In the upper right corner of the **service** box (not the resource box this time), click the Annotations (`@`) icon. 
1. Make sure BasePath is selected in the list, type `/myecho` in the text box, and then press Enter or click the + symbol to its right. 

The base path is now set, so that when you deploy this service, clients will be able to send requests to it using the URL http://localhost:9090/myecho.

### Change GET to POST

When you added the service, Ballerina configured the resource to use the GET method by default. Because we are going to use the incoming message to post a reply, let's change it to POST. 

1. Click the `@` symbol in the upper right corner of the resource box, highlight `GET`, and type `POST`. 
1. Click the `@` symbol again to hide the box. 

You can click the symbol again to confirm that GET was in fact changed to POST. You can also click the Source View button in the lower right corner to see the changes that are being made to the Ballerina code as you work with the visual editor.

### Add a function

Now, let's add a function that will take the incoming message and convert it to a response that gets sent back to the client. Because we are sending the original request back instead of composing a new message to send to the client, we need to strip its original headers before we reply, as those headers are intended for use in the client -> server direction and not the other way around. The `ballerina.net.http` package includes a native function called `convertToResponse` that removes the incoming HTTP headers when replying to the client. Let's add that function to our flow.

1. On the tool palette, go to the **ballerina.net.http** section, click the **convertToResponse** function, and drag it to the canvas below **Start**. 
1. Click in the parentheses of `convertToResponse()` and type `m`, which is the incoming message, so that it looks like this: `convertToResponse(m)`

### Add the reply
Now that we've added the function that will convert the incoming message text to a response, we just need to instruct the program to send the response back to the client.

1. On the tool palette, click the Reply icon ![alt text](../images/icons/reply.png "Reply icon") and drag it to the canvas under the `convertToResponse` function you just added. You'll see that it appears as a box with an arrow going back to the client. 
1. Click the reply box you just added, click the Edit icon, and set the response message to `m`, which instructs the program to send the message processed by the `convertToResponse` function back to the client. 

This completes the sequence, so you are now ready to save and run your integration program.

### Save the program

1. Click the **File** menu and choose **Save As**. 
1. Save it as `myEcho.bal` in your Ballerina `samples` directory. 

### Run the program

1. At the command prompt, navigate to your `<ballerina_home>/samples` directory. 
1. Enter the command to run the Ballerina server and deploy your myEcho program:

  ```
  ballerina run service ../samples/myEcho.bal
  ```

Your service is now deployed and running on the Ballerina server. 

### Send the request

From a separate command prompt, use curl to send a request to your program:

```
curl -v http://localhost:9090/echo2 -d "Hello World......"
```

The service receives the request, takes the text `Hello World......` from the incoming message, converts it into a response without the client headers, and sends it back to the command line where the request was sent.

You have now completed your first Ballerina program! If you run into problems, you can troubleshoot by comparing your code with the Echo sample.  

## Next steps

Now that you're familiar with running Ballerina in standalone and server mode, using the Composer to build an integration program, and creating a service and resource, you are ready to learn more. 

* Read the [Key Concepts](../key-concepts.md) page to familiarize yourself with the rest of the main features you need to know about.
* Browse through the [Samples](../samples.md) and use them as templates for your own programs.
* See the [Language Reference](../lang-ref/lang-overview.md) for complete information on using the Ballerina language. 
