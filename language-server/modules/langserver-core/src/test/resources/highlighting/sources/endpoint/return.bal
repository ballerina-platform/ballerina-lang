import ballerina/http;

public function functionName() returns http:Client{
    http:Client myClient = new("");
    return myClient;
}

