function main (string... args) {
    generateGre<caret>eting("Ballerina");
}

@Description {value:"Generates a greeting"}
@Param {value:"name: name of the person"}
@Return {value:"greeting message"}
function generateGreeting (string name) (string) {
    return "Hello " + name;
}
