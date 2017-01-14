package test.lang;

import ballerina.lang.json;
import ballerina.net.http;

function arrayIndexOutOfBoundTest() {
    string name;
    string[] animals;

    animals = ["Lion", "Cat"];
    name = animals[5];
    return;
}

function testStackTrace() {
  string[] fruits;
  string apple;
  apple = getFruit1(fruits);
}

function getFruit1(string[] fruits) (string) {
  return getFruit2(fruits);
}

function getFruit2(string[] fruits) (string) {
  return getApple(fruits);
}

function getApple(string[] fruits) (string) {
  return fruits[24];
}

function nativeFunctionErrorTest() (string) {
    json j;
    j = `{"name":"wso2"}`;
    return json:getString(j, "malformed/jsontpath/.");
}

function nativeConnectorErrorTest() {
	http:HTTPConnector endpoint = new http:HTTPConnector("malformed/url");
	message request;
	request = new message("test");
	http:HTTPConnector.get(endpoint, "/context", request);
}