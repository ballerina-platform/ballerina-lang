import ballerina.lang.strings;

function findAll(string regex, string inputText) (string[]){
    return strings:findAll(regex, inputText);
}

function match(string regex, string inputText) (boolean){
    return strings:match(regex, inputText);
}

function replaceAll(string inputText, string regex, string replacingText) (string){
    return strings:replaceAll(inputText, regex, replacingText);
}