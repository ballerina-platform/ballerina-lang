public const int CONST_ARR_LENGTH = 10;

// Simple Type Descriptor
public type SimpleTypeDesc int;
public type SimpleTypeDesc2 SimpleTypeDesc;

// List Type Descriptor
public type ArrayTypeDesc1 SimpleTypeDesc[10];
public type TupleTypeDescriptor [string, SimpleTypeDesc2, SimpleTypeDesc...];

// Map Type Descriptor
public type MapTypeDescriptor map<SimpleTypeDesc>;
type InclusiveRecordTypeDescriptor record {
    SimpleTypeDesc field1 = 1;
    int field2 = 2;
};
type ExclusiveRecordTypeDescriptor record {|
    SimpleTypeDesc field1 = 12;
    SimpleTypeDesc2...;
|};

// Function Type Descriptor
// TODO: Function Type Descriptor with rest param should be supported after compiler support
public type FunctionTypeDesc function (SimpleTypeDesc, string, SimpleTypeDesc2) returns SimpleTypeDesc2;

// Object Type Descriptor
public type ObjectTypeDesc object {
    SimpleTypeDesc intVal = 1;
};

// Future Type Descriptor
public type FutureTypeDesc future<SimpleTypeDesc>;

// Typedesc Type Descriptor
public type TypeDescriptorTypeDesc typedesc<SimpleTypeDesc>;

// Handle Type Descriptor
public type HandleTypeDesc handle;

// Support this after compiler supports this public type ArrayTypeDesc1 SimpleTypeDesc[CONST_ARR_LENGTH];

function testTyeDescriptors() {
    SimpleTypeDesc tDesc1;

    // ArrayTypeDesc1 tDesc2;
    SimpleTypeDesc[10] tDesc3;
    // TupleTypeDescriptor tDesc4;
    [string, SimpleTypeDesc2, SimpleTypeDesc...] tDesc5;

    MapTypeDescriptor tDesc6;
    map<SimpleTypeDesc> tDesc7;

    InclusiveRecordTypeDescriptor tDesc8;
    record { SimpleTypeDesc field1 = 1; int field2 = 2;} tDesc9;

    ExclusiveRecordTypeDescriptor tDesc10;
    record {|SimpleTypeDesc field1 = 12; SimpleTypeDesc2...;|} tDesc11;

    FunctionTypeDesc tDesc12;
    function (SimpleTypeDesc, string, SimpleTypeDesc2) returns SimpleTypeDesc2 tDesc13;

    ObjectTypeDesc tDesc14;
    object {
        SimpleTypeDesc intVal = 1;
    } tDesc15;

    FutureTypeDesc tDesc16;
    future<SimpleTypeDesc> tDesc17;
    
    TypeDescriptorTypeDesc tDesc18;
    typedesc<SimpleTypeDesc> tDesc19;

    HandleTypeDesc tDesc20;
    handle tDesc21;
}



type Person record {
    string fname = "";
    string lname = "";
};

type Employee record {
    *Person;
    string name;
};
