import ballerina/http;

http:Client myClient = new("");

var response = myClient->get("");
