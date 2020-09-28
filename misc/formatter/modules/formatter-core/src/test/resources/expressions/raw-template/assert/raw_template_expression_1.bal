import ballerina/lang.'object;

public function foo() {
    string name = "Ballerina";
    'object:RawTemplate template = `Hello ${name}!!!`;
}
