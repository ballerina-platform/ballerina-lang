import ballerina/lang.'value;
type Error0 distinct error;
type Error1 distinct error<map<value:Cloneable>>;
type Error2 distinct Error0|Error1;

type SomeUnion distinct Error0|Error1|record {| string key; string val; |};
type TheInt distinct int;
type TheTypeDesc distinct typedesc<int>;
