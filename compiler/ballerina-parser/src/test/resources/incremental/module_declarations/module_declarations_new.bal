import ballerina/io;

type Foo record {
    int a;
};

service HelloService on someListener {
}

string name = "John";

const status = "OK";

annotation MyAnnot on function;

xmlns "http://wso2.com/ns1" as ns1;

enum Direction {
    LEFT,
    RIGHT
}

public function updatedFoo() {
    int x;
    int y = 4;
}

public function bar() {
    int x;
}
