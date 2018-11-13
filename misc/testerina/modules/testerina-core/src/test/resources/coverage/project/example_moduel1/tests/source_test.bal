import ballerina/test;
import ballerina/io;

@test:BeforeSuite
function beforeSuiteFunc() {
    io:println("I'm the before suite function!");
}


function beforeFunc() {
    io:println("I'm the before function!");
}

@test:Config {
    before: "beforeFunc",
    after: "afterFunc"
}
function testFunction() {
    io:println("I'm in test function!");

    sourceFunc();

    test:assertTrue(true, msg = "Failed!");
}

function afterFunc() {
    io:println("I'm the after function!");
}

@test:AfterSuite
function afterSuiteFunc() {
    io:println("I'm the after suite function!");
}
