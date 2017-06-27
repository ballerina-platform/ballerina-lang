import ballerina.lang.strings;
import ballerina.lang.blobs;
import ballerina.lang.system;

function main(string[] args) {
    blob content;
    //Convert a string value into a blob value by providing the encoding to be used.
    content = strings:toBlob("Sample Text", "UTF-8");
    //Convert a blob value into a string value by providing the encoding.
    string str = blobs:toString(content, "UTF-8");
    //Print the converted string value
    system:println(str);
}