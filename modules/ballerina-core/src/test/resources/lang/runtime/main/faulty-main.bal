function main (string[] args) {
    int a = 0;
    test1(args);
    int b = 2;
}

function test1(string[] args){
    test2(args);
}

function test2(string[] args){
    test3(args);
}

function test3(string[] args){
    int a = 0;
    int b = 2;
    string value;
    while (a < 5) {
        if (b == a) {
            value = args[a];
        }
        a = a + 1;
    }
}