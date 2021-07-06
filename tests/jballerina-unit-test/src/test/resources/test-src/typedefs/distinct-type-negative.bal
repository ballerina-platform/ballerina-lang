import ballerina/lang.'value;
type Error0 distinct error;
type Error1 distinct error<map<value:Cloneable>>;
type Error2 distinct (Error0|Error1);

type SomeUnion distinct (Error0|Error1|record {| string key; string val; |});
type TheInt distinct int;
type TheTypeDesc distinct typedesc<int>;


type Person record {
    distinct record { int i; } value;
    distinct int code;
    distinct readonly & int origCode;
    int | distinct record { int i; } uniVal;
    record { distinct record { int code; } rec; } rec;
};

type PersonObj object {
    distinct record { int i; } value;
    distinct int code;
    distinct readonly & int origCode;
    int | distinct record { int i; } uniVal;
    record { distinct record { int code; } rec; } rec;
};

class PersonClass {
    distinct record { int i; } value;
    distinct int code;
    distinct readonly & int origCode;
    int | distinct record { int i; } uniVal;
    record { distinct record { int code; } rec; } rec;
}
