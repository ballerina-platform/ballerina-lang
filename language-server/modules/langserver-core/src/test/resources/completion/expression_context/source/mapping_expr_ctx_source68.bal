type A record {|
    int field1;
    string field2;
    int field3;
|};

type B record {|
    int field1;
    string field2;
    int field3;
|};

public function main() {
    A a = {
        field1: 10,
        field2: "",
        field3: 30
    };

    B b = {field1: 20, };
}
