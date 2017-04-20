package com.test;

import ballerina.doc;

function main (string[] args) {
    getGreetingMess<caret>age("Ballerina", "Hello");
}

@doc:Description {value:"Generates a greeting"}
@doc:Param {value:"name: name of the person"}
@doc:Param {value:"greeting: greeting"}
@doc:Return {value:"complete greeting message"}
function getGreetingMessage (string name, string greeting) (string) {
    return greeting + " " + name;
}
