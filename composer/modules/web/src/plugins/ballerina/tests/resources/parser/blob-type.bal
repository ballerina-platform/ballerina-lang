import ballerina/lang.strings;
import ballerina/lang.blobs;
import ballerina/lang.system;

function main (string... args) {
    //Convert a string value to blob by providing the encoding to be used.
    blob content = strings:toBlob("Sample Text", "UTF-8");

    //Convert a blob value into a string value by providing the encoding.
    string str = blobs:toString(content, "UTF-8");
    system:println(str);
}