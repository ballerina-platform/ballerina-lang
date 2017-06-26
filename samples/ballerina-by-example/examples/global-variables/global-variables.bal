import ballerina.lang.system;

@doc:Description{value : "This is a global level int variable."}
int globalInt = 98;

@doc:Description{value : "This is a global level string variable."}
string globalString = "string value";

function main (string[] args) {

    //Accessing global level int variable.
    system:println("global int - " + globalInt);
    //Accessing global level string variable.
    system:println("global string - " + globalString);

    //Changing global level variable value within the function.
    globalInt = globalInt + 66;

    system:println("global int changed - " + globalInt);
}
