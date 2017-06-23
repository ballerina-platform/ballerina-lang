import ballerina.lang.system;

@doc:description{value : "This is a global level int variable"}
int globalInt = 98;

@doc:description{value : "This is a global level string variable"}
string globalString = "string value";

function main (string[] args) {

    //Accessing global level int variable.
    system:println("global int - " + globalInt);
    system:println("global string - " + globalString);

    //Changing global level variable value within the function.
    globalInt = globalInt + 66;

    system:println("global int changed - " + globalInt);
}
