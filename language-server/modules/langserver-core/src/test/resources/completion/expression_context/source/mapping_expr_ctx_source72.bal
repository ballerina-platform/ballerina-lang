type A record {
    int field1;
    string field2;
};

type B record {|
    int field1;
    string field2;
    int field3;
|};

public function main() {
    A a = {
        field1: 10,
        field2: ""
    };

    B b = {};
}

function getB(int field1, string field2, string field3) returns B {
    return {
        field1: 0,
        field2: "",
        field3: 0
    };
}

function getA(int field1, string field2) returns A {
    return {
        field1: 0,
        field2: ""
    };
}
