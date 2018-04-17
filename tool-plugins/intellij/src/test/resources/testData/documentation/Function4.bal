package com.test;

function main (string... args) {
    getGreetingMess<caret>age("Ballerina", "Hello");
}

@Description {value:"Generates a greeting"}

@Param {value:"name: name of the person"}

@Param {value:"greeting: greeting"}

@Return {value:"greetingMessage: complete greeting message"}

function getGreetingMessage (string name, string greeting) (string greetingMessage) {
    greetingMessage = greeting + " " + name;
}
