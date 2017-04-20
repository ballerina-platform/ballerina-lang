function main (string[] args) {
    Pers<caret>on person = {};
}

@doc:Description {value:"Represents a person"}
@doc:Field {value:"name: name of the person"}
@doc:Field {value:"age: age of the person"}
struct Person {
    string name;
    int age;
}
