# Quick Tour

Now that you know [a little bit about Ballerina](index.md), let's take it for a spin! 

## Install Ballerina

1. Go to [http://www.ballerinalang.org](http://www.ballerinalang.org) and click **Download**. 
1. Download the Ballerina Tools distribution and unzip it on your computer. Ballerina Tools includes the Ballerina runtime plus the visual editor (Composer) and other tools. 
1. Add the `<ballerina_home>/bin` directory to your $PATH environment variable so that you can run the Ballerina commands from anywhere. 

>NOTE: Throughout this documentation, `<ballerina_home>` refers to the Ballerina directory you just installed. 

## Run HelloWorld

The HelloWorld sample will show you how easy it is to run Ballerina, send it a request, and get a response. 

Let's take a look at what the sample looks like in the Ballerina programming language:

```
import ballerina.lang.system;
function main (string[] args) {
  system:println("Hello, World!");
}
``` 

The HelloWorld sample doesn't take any specific input, so simply running it will cause it to print "Hello, World!" at the command line.

At the command prompt, navigate to the `<ballerina_home>/samples/helloWorld` directory and enter the following line:

```
ballerina run main helloworld.bal
```

You will see the following response:

```
Hello, World!
```

You just started Ballerina, ran the HelloWorld sample, and got a response within seconds. 

Pretty simple and straightforward, right? Now, let's look at something a little more interesting: the Composer.

## Why Ballerina Composer

The Ballerina Composer provides a flexible and powerful browser-based tool for creating your Ballerina programs. This is a revolutionary way of doing programming for integration due to its use of sequence diagrams, enabling you to architecturally generate your code while designing your solution. The Ballerina Composer sets Ballerina apart from other integration paradigms due to its unique visual representation.

You can build your integrations by creating sequence diagrams, dragging elements from a tool palette onto a canvas. As you build the diagrams, the underlying code is written for you, which you can work with in the Source view. You can also use the Swagger view to define services by writing Swagger definitions. You can switch seamlessly between the Design view, Source view, and Swagger view and create your programs in the way that you like to work.

## Run the Composer

1. At the command prompt, type `composer`.

1. Access the Composer from the following URL in your browser: http://localhost:9091

    The welcome page of Ballerina Composer appears. 
    
    ![alt text](images/WelcomePage.png "Welcome page")
    
    Let's open a sample and take a look around. 

## Explore the Ballerina Composer

Once you have accessed the composer, you can have a look around using the available samples in the welcome page of the Ballerina Composer.

1. Click **echoService**.

    The Echo Service program displays in the Composer.
    
    ![alt text](images/EchoDesign.png "Echo Service program")

    Notice that on the left you have a tool palette containing the various constructs that you'll use to build your integration. On the right, you have a visual editor with a canvas onto which you drag those constructs. This is where you build your sequence diagrams that define your integration logic. 

    Also notice the **Source View** and **Swagger View** buttons in the lower right corner. 
   
2. Click **Source View**. 

    ![alt text](images/EchoSource.png "Source view")

    You'll see the source code editor that represents the sequence diagram as code in the Ballerina language. You can go back and forth between the visual editor and the source code and make your edits in either place. 

3. Click **Swagger View**. 

    ![alt text](images/EchoSwagger.png "Swagger view")

    If your Ballerina program contains services and resources, you can view the generated Swagger definition for your program by switching to the Swagger view. This editor allows you to write Swagger definitions to create services. All the changes made on the Swagger definition will reflect on the Ballerina program when you switch back to Source or Design view.

1. Click **Design View** to return to the visual editor. If you happen to write the source code completely in the source view, when you switch to Design view, the Visual representation is created.

1. Click the "x" to the right of "untitled" in the tab title to close this sample, and click **Don't Save** when prompted.

Now that you've had a look around, it's time to take Ballerina for a spin and [write your first Ballerina program](tutorials/first-program.md).
