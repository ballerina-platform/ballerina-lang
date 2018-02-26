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
function main (string[] args) {
  println("Hello, World!");
``` 

The HelloWorld sample doesn't take any specific input, so simply running it will cause it to print "Hello, World!" at the command line.

At the command prompt, navigate to the `<ballerina_home>/samples/helloWorld` directory and enter the following line:

```bash
ballerina run helloworld.bal
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
    
    ![alt text](images/ComposerNew1.png "Welcome page")
    
Let's open a sample and take a look around. 

## Explore the Ballerina Composer

Once you have accessed the composer, you can have a look around using the available samples in the welcome page of the Ballerina Composer.

1. Click **Echo Service**.

    The Echo Service program displays in the Composer.
    
    ![alt text](images/echoServiceNew1.png "Echo Service program")

    Notice that on the top you have an add button containing the various constructs that you'll use to build your integration. When you select a construct it is drawn in the canvas. This is where you build your sequence diagrams that define your integration logic.
        
    ![alt text](images/ConstructsNew1.png "Constructs")
    
    Some constructs have a **life line** of execution where you program the statements to be executed. This defines the flow of execution. The life line is represented by a vertical line in the default program or any other construct of the **echoService**.
    
    ![alt text](images/LifelineNew1.png "Ballerina construct life line")
    
    There are packages called **Connectors** and **Libraries** that are available to be used by your Ballerina program. By default, the Composer imports few commonly used packages to this section. If you add an import to a different package, it is added to this section. Click on the plus icon on the right of the diagram and select Endpoint to see the list of connectors.
    
    ![alt text](images/Connectors.png "Ballerina connectors and libraries")

    Notice the **Source View** button and the **Split View** button in the lower right corner. Also note the **Swagger Source** button associated with the service.
    
    ![alt text](images/SourceSwaggerButtons1.png "Source and Swagger buttons")
   
2. Click **Source View**. 

    ![alt text](images/EchoSourceNew1.png "Source view")

    You'll see the source code editor that represents the sequence diagram as code in the Ballerina language. You can go back and forth between the visual editor and the source code and make your edits in either place. This can be done by clicking **Design View** in the lower right corner.

3. Click **Swagger View**. 

    ![alt text](images/EchoSwaggerNew1.png "Swagger view")

    If your Ballerina program contains services and resources, you can view the generated Swagger definition for your program by switching to the Swagger view. This editor allows you to write Swagger definitions to create services. All the changes made on the Swagger definition will reflect on the Ballerina program when you switch back to Source or Design view.

4. Click **Design View** to return to the visual editor. If you happen to write the source code completely in the source view, when you switch to Design view, the Visual representation is created.

5. You can run your program from the Composer itself. Click the **Run** button on the left of the Composer and choose whether you want to run your program as an **Application** or a **Service**. You can stop the application by clicking **Stop Application**.

    ![alt text](images/BallerinaRunNew1.png "Run application")

6. Click the "x" to the right of "echoService.bal" in the tab title to close this sample, and click **Don't Save** when prompted.

