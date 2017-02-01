==========
Quick Tour
==========

Now that you know :doc:`a little bit about Ballerina <intro>`, let's take it for a spin! 

------------------
Install Ballerina
------------------
Download Ballerina from http://www.ballerinalang.org and unzip it on your computer. 

--------------
Run HelloWorld
--------------
This sample will show you how easy it is to run Ballerina, send it a request, and get a response. The HelloWorld sample doesn't take any specific input, so simply running it will cause it to print "Hello, World!" at the command line.

At the command prompt, enter the following line::

  ballerina run helloworld.bal

You will see the following response::

  Hello, World!
  
You just started Ballerina, ran the HelloWorld sample, and got a response within seconds. Let's take a look at what the sample looks like in the Ballerina programming language::

  function main (string[] args) {
    system:println("Hello, World!");
  }

Pretty simple and straightforward, right? Now, let's look at something a little more interesting. 

.. Show a sample that requires input, then show how to run the editor and walk through that
