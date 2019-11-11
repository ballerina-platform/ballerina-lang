public const int CONST_ARR_LENGTH = 10;

public type SimpleTypeDesc int;
public type SimpleTypeDesc2 float;

public type ArrayTypeDesc1 SimpleTypeDesc[10];
public type TupleTypeDescriptor [string, SimpleTypeDesc2, SimpleTypeDesc...];

public type MapTypeDescriptor map<SimpleTypeDesc>;
type InclusiveRecordTypeDescriptor record {
    int field1 = 1;
    int field2 = 2;
};
type ExclusiveRecordTypeDescriptor record {|
    int field1 = 12;
    string...;
|};

// Support this after compiler supports this public type ArrayTypeDesc1 SimpleTypeDesc[CONST_ARR_LENGTH];

function testTyeDescriptors() {
    SimpleTypeDesc tDesc1;
    ArrayTypeDesc1 tDesc2;
    TupleTypeDescriptor tDesc3;
    MapTypeDescriptor tDesc4;
    InclusiveRecordTypeDescriptor tDesc5;
    ExclusiveRecordTypeDescriptor tDesc6;
}
