import ballerina.lang.system;
import ballerina.doc;
@doc:Description {value: "This is a global variable declaration."}
int total = 98;

string content = "";

function main (string[] args) {

    //This is a means of accessing a global variable.
    system:println(total);

    content = content + "This is a sample text\n";
    system:println(content);
}
