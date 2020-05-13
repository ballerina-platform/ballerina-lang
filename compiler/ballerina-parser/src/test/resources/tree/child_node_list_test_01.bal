import ballerina/io;

public function add(int x, int y) returns int {
    int a;
    int b;
    int c;
    a = x;
    b = y;
    c = a + b;
    return c;
}

function sub(int x, int y) {
    int a;
    int b;
    int c;
    a = x;
    b = y;
    c = a + b;
    return c;
}

type Calculator object {
    function add(int x, int y) {
         int a;
         int b;
         int c;
         a = x;
         b = y;
         c = a + b;
    }

    function add(int x, int y) {
         int a;
         int b;
         int c;
         a = x;
         b = y;
         c = a + b;
    }
};