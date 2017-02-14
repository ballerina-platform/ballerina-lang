# Quick Tour

Now that you know [a little bit about Ballerina](intro.md), let's take it for a spin! 

## Install Ballerina

Download Ballerina from http://www.ballerinalang.org and unzip it on your computer. 

## Run HelloWorld

This sample will show you how easy it is to run Ballerina, send it a request, and get a response. The HelloWorld sample doesn't take any specific input, so simply running it will cause it to print "Hello, World!" at the command line.

At the command prompt, navigate to the `<ballerina_home>/bin` directory and enter the following line:

```
ballerina run helloworld.bal
```

You will see the following response:

```
Hello, World!
```

You just started Ballerina, ran the HelloWorld sample, and got a response within seconds. Let's take a look at what the sample looks like in the Ballerina programming language:

```
function main (string[] args) {
  system:println("Hello, World!");
}
```

Pretty simple and straightforward, right? Now, let's look at something a little more interesting: the editor.

## Run the editor

1. In the `bin` directory, type `sh editor` at the command prompt (omit `sh` if you are running on Windows).

1. Access the editor from the following URL in your browser: http://localhost:9091

   The welcome page of the Ballerina editor appears. Let's open a sample and take a look around. 

1. Click **echoService**.

Notice that on the left you have a tool palette containing the various constructs that you'll use to build your integration. On the right, you have a visual editor with a canvas onto which you drag those constructs. This is where you build your sequence diagrams that define your integration logic. 

Also notice in the lower right corner the Source View and Swagger View buttons. 
   
1. Click **Source View**. You'll see the source code editor that represents the sequence diagram as code in the Ballerina language. You can go back and forth between the visual editor and the source code and make your edits in either place. 

1. Click **Swagger View**. This editor allows you to create Swagger definitions to create APIs.

1. Click **Design View** to return to the visual editor. 

Now that you've had a look around, it's time to take the leap into [creating your first Ballerina program](tutorials/first-program.md).
