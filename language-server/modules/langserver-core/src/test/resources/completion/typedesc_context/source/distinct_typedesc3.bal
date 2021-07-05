import ballerina/module1;

public type TestRecord record {
    string foo;
    int bar?;
};

public annotation typeAnnotation1 on type;

type E1 error<map<anydata>>;

type E2 E1;

class Class1 {
    
}

type C1 Class1;

type O1 object {};

type O2 O1;

type DistinctType distinct module1:
