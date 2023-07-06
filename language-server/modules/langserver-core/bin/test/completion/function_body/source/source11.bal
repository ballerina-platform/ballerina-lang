import ballerina/module1 as mod1;

type Foo record {|
    xml x;
|};

public function main() returns error? {
    json j = {};

    mod1:
    
    Foo|error r1;
    Foo|error r2 = j.fromJsonWithType();
}
