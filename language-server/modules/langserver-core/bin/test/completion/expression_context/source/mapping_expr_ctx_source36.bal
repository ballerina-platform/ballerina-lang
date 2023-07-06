import ballerina/module1;

type Foo record {|
    int f1;
    string f2?;
|};

module1:TestRecord1 rec = {}
