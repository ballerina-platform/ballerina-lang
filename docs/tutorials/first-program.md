# Write your First Program

Now that you’ve taken the [Quick Tour](../quick-tour.md), let's dig a little deeper and write your first Ballerina program. This tutorial will teach you how to run Ballerina in standalone and server mode, use the editor to build your program, understand the key concepts, and get familiar with the Ballerina language.

## Run Ballerina

In the [Quick Tour](../quick-tour.md), you learned how to start Ballerina and run a sample program from the `<ballerina_home>/samples/helloWorld` directory with a single command:

```bash
ballerina run helloWorld.bal
```

After the HelloWorld program executed, Ballerina stopped. This approach is called **standalone mode**, and it's useful when you want to execute a program once and then stop as soon as it has finished its job. It runs the `main()` function of the program you specify and then exits. 

You can also run Ballerina as a **server**, so that it can deploy one or more services that wait for requests. To see how this works, let's go to your `<ballerina_home>/samples/helloWorldService` directory, and then run Ballerina in server mode and deploy the HelloWorldService program:

```bash
ballerina run helloWorldService.bal
```

In this case, Ballerina ran and deployed the HelloWorldService program as a service, which is waiting for a request. Let's send it one now. The Ballerina server is available at `localhost:9090`, and HelloWorldService is available at context `hello`. Open another command line window and use the [cURL](https://curl.haxx.se) client to call the service as follows:

```bash
curl -v http://localhost:9090/hello
```

The service receives the request and executes its logic, printing "Hello, World!" on the command line. Notice that the Ballerina server is still running in the background, waiting for more requests to come in. You can stop the Ballerina server by pressing Ctrl-C. 

## Create a new program

It's time to create your first program! In this exercise, we are going to create a service that takes an incoming message, extracts the text, and sends a message back to the client with that same text. 

### Add a service and resource

First, we add a **service** construct to the canvas in the Composer. A service is a container for all the other constructs and represents a single unit of functionality that can be accessed remotely.

1. If the Composer is not already running, run it as described in the [Quick Tour](../quick-tour.md).
1. On the Welcome page, click **New**.
1. On the tool palette, click the service icon and drag it to the canvas. 

![alt text](../images/AddService.gif)

A box appears called `Resource1` with some content already created for you. If you scroll up, you can see that it is inside another box called `Service1`, which is the top-level container for the service.  

A **resource** is a single request handler within a service. This is where we will program the logic describing how to handle certain types of requests from incoming messages to this service. The diagram shows that when clients send messages to this service, they are received by the `Resource1` resource, which has one **worker** that handles the message. Each worker is a separate thread of execution, so you can add more workers if you want to do parallel processing. In this tutorial, we'll just use the default worker.  

By default, the resource is configured to accept a message `m`. You can see this by clicking the Arguments icon in the upper right corner of the resource box: 

![alt text](../images/icons/arguments.png "Arguments icon")

When you click it, you'll see `message m` listed below the fields. Click the Arguments icon again to close its window.

Let's rename the service and resource. 

1. Highlight the name `Service1` and type `myEchoService` in its place. 
1. Change `Resource1` to `myEchoResource` in the same way. 

### Set the base path

The base path for a service is the context portion of the URL that clients will use to send requests to this service. For example, if we set the base path to `/myecho`, clients will be able to send requests to this service using the URL http://localhost:9090/myecho. Let's do this now.

1. Click the add annotation textbox at the left top corner of service
1. Select ballerina.net.http from the dropdown that appears.
1. Select config. You can see a little + sign within curly braces. Click that and select basePath.
1. Add the basePath and in front of it, edit the value. 

> Make sure you add the base path with the /.

The base path is now set, so that when you deploy this service, clients will be able to send requests to it using http://localhost:9090/myecho.

### Change GET to POST

When you added the service, Ballerina configured the resource to use the GET method by default. Because we are going to use the incoming message to post a reply, let's change it to POST. 

1. Click the Annotations (@) icon in the upper right corner of the myEcho**Resource** box (not the service box).
1. Click `http:GET`, highlight `GET`, and type `POST`. 
1. Click the Annotations icon again to hide the box. 

You can click the symbol again to confirm that GET was in fact changed to POST. You can also click the **Source View** button in the lower right corner to see the changes that are being made to the Ballerina code as you work with the visual editor. Currently, the source code should look like this:

```Ballerina
import ballerina.net.http;

@http:configuration {basePath:"/myecho"}
service<http> echo {

    @http:resourceConfig {
        methods:["POST"],
        path:"/"
    }
    resource echo (http:Request req, http:Response resp) {
  
    }
}
```

### Add a function

Now, let's add a function that will take the incoming message and convert it to a response that gets sent back to the client. Because we are sending the original request back instead of composing a new message to send to the client, we need to strip its original headers before we reply, as those headers are intended for use in the client -> server direction and not the other way around. The `ballerina.net.http` package includes a native function called `convertToResponse` that removes the incoming HTTP headers when replying to the client. Let's add that function to our flow.

1. Click **Design View**. 
1. On the tool palette, scroll down and click the **ballerina.net.http** section to expand it, click the **convertToResponse** function, and drag it to the canvas below **start**. 
1. Highlight `message` in `http:convertToResponse(message)` and replace it with `m`, which is the incoming message, so that it looks like this: `http:convertToResponse(m)`

### Add the reply
Now that we've added the function that will convert the incoming message text to a response, we just need to instruct the program to send the response back to the client. We will use the Reply icon in the tool palette, which looks like this:

![alt text](../images/icons/reply.png "Reply icon")

1. On the tool palette, click the Reply icon and drag it to the canvas under the `convertToResponse` function you just added. 

    It appears as a box with an arrow pointing back to the client. 

1. Click the reply box you just added and type `m` to instruct the program to send the message processed by the `convertToResponse` function back to the client. Click outside of the box. 

This completes the sequence. If you go to the Source view, your program should now look like this:

```Ballerina
import ballerina.net.http;

@http:configuration {basePath:"/myecho"}
service<http> echo {

    @http:resourceConfig {
        methods:["POST"],
        path:"/"
    }
    resource echo (http:Request req, http:Response resp) {
        string payload = req.getStringPayload();
        resp.setStringPayload(payload);
        resp.send();
    }
}
```

You are now ready to save and run your integration program

### Save the program

1. Click the **File** menu and choose **Save As**. 
1. Specify the name as `myEcho.bal` and specify the location as your Ballerina `samples` directory. You can type the path directly in the Location box, or use the tree to navigate to the directory. 
1. Click **Save**.

### Run the program

You can run a Ballerina program from inside the Composer using the Run icon, which is to the left of the tool palette:

![alt text](../images/icons/run.png "Run icon")

Click the Run icon now and click **Service**. If you were creating a Ballerina program with a `main()` function, you would click **Application** instead.

Your service is now deployed and running on the Ballerina server. 

### Send the request

In a new command prompt, use cURL to send the following request to your program:

```Bash
curl -v http://localhost:9090/myecho/myEchoResource -d "Hello World......"
```

The service receives the request, takes the text `Hello World......` from the incoming message, converts it into a response without the client headers, and sends it back to the command line where the request was sent.

You have now completed your first Ballerina program! If you run into problems, you can troubleshoot by comparing your code with the Echo sample.

## Next steps

Now that you're familiar with running Ballerina in standalone and server mode, using the Composer to build a program, and creating a service and resource, you are ready to learn more. 

* Read the [Key Concepts](../key-concepts.md) page to familiarize yourself with the rest of the primary features you need to know about.
* Read about the [Tools](../tools.md) that you can use with Ballerina, such as using an IDE instead of the Composer. 
* Run through the rest of the [Tutorials](index.md) to get hands-on experience.  
