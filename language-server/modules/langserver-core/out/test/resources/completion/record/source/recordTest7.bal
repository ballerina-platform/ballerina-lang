type Person2 record {


};

type Person1 record {|
    string field1 = "";
    boolean field2 = true;
|};

function testFunction() {
    Person1|Person2|int p = {field1: 12.0, field2:12};
    string|float testVar = p.
}