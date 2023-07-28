import ballerina/module1;

public type Type1 record {|
    module1:TestRecord1 field1;
    module1:TestRecord1 field2;
    int field2;
|};

public type Type2 record {|
     module1:TestRecord1 field1;
     float field2;
|};



public function main() {
    Type1|module1:TestRecord1|Type2 rec = {};
}
