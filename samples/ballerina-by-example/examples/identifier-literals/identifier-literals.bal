import ballerina.lang.system;

@doc:Description{value : "Global variable defined using identifier literal."}
string |this is a global variable| = "global var with identifier literal";

function main (string[] args) {

    //Local variable defined using identifier literal.
    string |sample variable| = "identifier literal";

    //Invoking a function with identifier literal as a parameter.
    |this is a sample function|(|sample variable|);

    //Using global variable defined using identifier literal as function parameter.
    |this is a sample function|(|this is a global variable|);
}

@doc:Description{value : "Sample function defined with function name and input parameter as identifier literals."}
function |this is a sample function| (string |input value|){
    system:println(|input value|);
}
