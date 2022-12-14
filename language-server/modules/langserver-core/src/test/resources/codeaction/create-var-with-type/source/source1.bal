import ballerina/module1;
import ballerina/jballerina.java;

module1:Client 'client = new (url="http://ballerina.io");

function testRemoteMethodCallAction1() {
    'client->get(path="/path");
}

function testRemoteMethodCallAction2() {
    handle handleObj = java:fromString("String");
    'client->forward("/path", handleObj);
}

function testResourceAccessAction() {
    'client->/["path3"]();
}
