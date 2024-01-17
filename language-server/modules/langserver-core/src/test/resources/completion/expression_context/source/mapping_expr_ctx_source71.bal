type A record {|
    int field1;
    string field2;
    int field3;
|};

type B record {|
    int field0;
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

    B b = {field0: 20, };
}
