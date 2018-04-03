import ballerina/io;

function main (string[] args) {
    //Convert a string value to blob by providing the encoding to be used.
    string text = "Sample Text";
    blob content = text.toBlob("UTF-8");

    //Convert a blob value into a string value by providing the encoding.
    string str = content.toString("UTF-8");
    io:println(str);
}
