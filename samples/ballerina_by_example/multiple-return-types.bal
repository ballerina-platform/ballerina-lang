import ballerina.lang.system;

//demonstrates how multiple return values work.

function main (string[] args) {
    string x;
    int y;
    string k;
    x,y,k = multiValueReturn("hello");
    system:println(x);
    system:println(y);
    system:println(k);
}

function multiValueReturn(string s) (string, int, string){
    return s, 2, "ballerina";
}
