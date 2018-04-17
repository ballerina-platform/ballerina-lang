import ballerina/io;

function main (string... args) {
    //Convert the value type string to blob by providing the encoding.
    string text = "Sample Text";
    blob content = text.toBlob("UTF-8");

    //Convert the value type blob to string by providing the encoding.
    string str = content.toString("UTF-8");
    io:println(str);
}
