import ballerina/module1;

type Data record {|
    string name?;
|};

annotation Data TAnnot on field;

annotation Data ServAnnot on service;

type MyTuple [@module1: int, string];

