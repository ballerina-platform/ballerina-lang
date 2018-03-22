import ballerina/test.incorrect;

function incorrectParamCountFunction(string s1) (string){
    return incorrect:paramCount(s1);
}