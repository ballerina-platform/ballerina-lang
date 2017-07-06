import ballerina.lang.regex;

function findAll(string regex, string inputText) (string[]){
    return regex:findAll(regex, inputText);
}

function match(string regex, string inputText) (boolean){
    return regex:match(regex, inputText);
}

function replaceAll(string inputText, string regex, string replacingText) (string){
    return regex:replaceAll(inputText, regex, replacingText);
}