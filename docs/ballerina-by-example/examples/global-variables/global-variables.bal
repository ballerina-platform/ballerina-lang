import ballerina/io;

@Description {value:"Defining the global variables."}
int total = 98;

string content = "";

function main (string[] args) {

    // Accessing a global variable.
    io:println(total);

    content = content + "This is a sample text";
    io:println(content);
}
