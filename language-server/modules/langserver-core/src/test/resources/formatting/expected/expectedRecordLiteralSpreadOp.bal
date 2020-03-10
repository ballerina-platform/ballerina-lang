import ballerina/io;

type Foo record {|
    string s;
    int i;
    float f;
    boolean|float...;
|};

type Bar record {|
    string s;
    int i;
|};

public function main() {
    map<float> m1 = {q: 1.0, w: 2.0};
    map<boolean|float> m2 = {
        a: true,
        ...m1
    };    // spreads `m1`

    io:println("-------- m2 --------");
    foreach var [k, v] in m2.entries() {
        io:println("key: ", k, ", value: ", v);
    }

    function () returns map<boolean|float> func = () => m2;
    Bar b = {s: "str", i: 1};
    Foo f = {...b, f: 123.4, ...func()};    // spreads `b` and `m2` returned by `func`


    io:println("\n-------- f --------");
    foreach var [k, v] in f.entries() {
        io:println("key: ", k, ", value: ", v);
    }
}
