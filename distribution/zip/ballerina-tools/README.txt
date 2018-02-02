                            Ballerina Tools Distribution

The Ballerina Tools distribution contains a complete development kit for developing and running programs using 
the Ballerina language.

This distribution contains:

- Ballerina Runtime
    https://github.com/ballerinalang/ballerina
- Ballerina Composer
    https://github.com/ballerinalang/composer
- Testerina
    https://github.com/ballerinalang/testerina
- Docerina
    https://github.com/ballerinalang/docerina
    Docerina uses HTML5 UP templates for documents - https://html5up.net/
- Container Support
    https://github.com/ballerinalang/container-support
- Connectors
    https://github.com/ballerinalang/connectors

Getting started
====================

You can download the Ballerina Tools distribution, try samples, and read the documentation at http://ballerinalang.org.

Building from the source
============================

If you want to build Ballerina Tools from the source code:

1. Get a clone or download the source from this repository (https://github.com/ballerinalang/distribution).
2. Run the following Maven command from the root Ballerina Tools directory: mvn clean install
3. Extract the Ballerina Tools distribution created at `target/ballerina-tools-<VERSION>.zip` to your local directory.

Running samples
==================================

The website http://ballerinalang.org has several samples you can try out right there in the page. These samples are
also available in the `<ballerina_tools_home>/samples` directory in your Ballerina distribution.


Run HelloWorld
--------------

The HelloWorld sample will show you how easy it is to run Ballerina, send it a request, and get a response. Let's take a 
look at what the sample looks like in the Ballerina programming language:

function main (string[] args) {
  println("Hello, World!");
}

The HelloWorld sample doesn't take any specific input, so simply running it will cause it to print "Hello, World!" 
at the command line. Let's run the sample:

1. Add the <ballerina_tools_home>/bin directory to your $PATH environment variable. 
2. At the command prompt, navigate to the <ballerina_tools_home>/samples/helloWorld directory and enter the following line:

ballerina run helloWorld.bal

You will see the following response:

Hello, World!

You just started Ballerina, ran the HelloWorld sample, and got a response within seconds. Pretty simple and 
straightforward, right? Now, let's look at something a little more interesting: the Composer.


Run the Composer
----------------

1. Add the `<ballerina_tools_home>/bin` directory to your $PATH environment variable.
2. At the command prompt, type: composer
3. Access the Composer from the following URL in your browser: http://localhost:9091

   The welcome page of Ballerina Composer appears. Let's open a sample and take a look around.

3. Click echoService.

Notice that on the left you have a tool palette containing the various constructs that you'll use to build your
integration. On the right, you have a visual editor with a canvas onto which you drag those constructs.
This is where you build your sequence diagrams that define your integration logic.

Also notice the Source View and Swagger View buttons in the lower right corner.

* Click Source View. You'll see the source code editor that represents the sequence diagram as code in the
Ballerina language. You can go back and forth between the visual editor and the source code and make your edits
in either place.
* Click Swagger View. This editor allows you to write Swagger definitions to create services.
* Click Design View to return to the visual editor.
