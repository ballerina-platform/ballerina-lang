import ballerina.lang.system;

function main (string[] args) {
    int x;
    x = 0;
    //loops if condition true.
    while(x <= 5) {
        x = x + 1;
    }
    system:println(x);
}