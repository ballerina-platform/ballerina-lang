import ballerina.doc;

function main (string[] args) {
    generateGre<caret>eting("Ballerina");
}

@doc:Description {value:"Generates a greeting"}
@doc:Param {value:"name: name of the person"}
@doc:Return {value:"greeting message"}
function generateGreeting (string name) (string) {
    return "Hello " + name;
}
