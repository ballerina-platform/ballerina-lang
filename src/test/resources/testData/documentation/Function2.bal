import ballerina.doc;

function main (string[] args) {
    generateGre<caret>eting("Ballerina");
}

@doc:Description {value:"Generates a greeting"}
@doc:Param {value:"name: name of the person"}
@doc:Return {value:"greeting: greeting message"}
function generateGreeting (string name) (string greeting) {
    return "Hello " + name;
}
