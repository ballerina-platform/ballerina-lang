# Samples

The best way to start writing your Ballerina program is to use the samples as templates. Each sample includes a walk-through of the code so you can see how it's done. 

* **helloWorld**: A simple executable program that prints "Hello World" to the command line.
* **helloWorldService**: A service that prints "Hello World" to the command line.
* **echoService**: A service that takes text from the incoming request message and sends it back to the client as a response.
* **passThroughService**: A service that sends the incoming request message to a backend service and sends a response back to the client.
* **restfulService**: A RESTful ecommerce service that defines three resources and illustrates how you can build the business logic for each resource.
* **routingServices**: Contains two separate services that route messages to different backends based on the message content or header value.
* **serviceChaining**: Contains a composite service, ATMLocatorService, which illustrates how to chain services together to get the required information. It calls one service to find the nearest ATM by ZIP code, it calls a second service to get the address of that ATM, and then it composes the response and sends the information back to the client. 
* **twitterConnector**: An executable program that defines a connector that you can use to connect to a Twitter account and post a status update (tweet).
* **tweetOpenPR**: An executable program that uses the GitHub API to get the open pull requests for a specific repository and then tweets the total number of pull requests on Twitter.
* **tweetMediumFeed**: An executable program that retrieves the feed for WSO2 from Medium.com and tweets the first item's title. 
