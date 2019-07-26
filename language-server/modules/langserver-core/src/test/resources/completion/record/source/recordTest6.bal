type Person2 record {
    float field11 = 12.0;
    int field22 = 12;
};

type Person1 record {|
    string field1 = "";
    boolean field2 = true;
|};

function testFunction() {
    Person1|Person2|int p = {field1: 12.0, field2:12};
    string|float testVar = p.
}