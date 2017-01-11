package test.lang;

import ballerina.lang.json;
import ballerina.net.http;

function arrayIndexOutOfBoundTest() {
    string[] animals;
    animals = ["Lion", "Cat"];
    return animals[5];
}

function testStackTrace() {
  string[] fruits;
  string apple;
  apple = getFruit1(fruits);
}

function getFruit1(string[] fruits) {
  return getFruit2(fruits);
}

function getFruit2(string[] fruits) {
  return getApple(fruits);
}

function getApple(string[] fruits) {
  return fruits[24];
}

function nativeFunctionErrorTest() {
    json j;
    j = `{"name":"wso2"}`;
    return json:getString(j, "malformed/jsontpath/.");
}