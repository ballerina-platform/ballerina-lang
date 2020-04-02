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
    map<boolean|float> m2 = {a: true, ...m1};

    function () returns map<boolean|float> func = () => m2;
    Bar b = {s: "str", i: 1};
    Foo f = {...b, f: 123.4, ..};
}
