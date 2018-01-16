function main (string[] args) {
    Pers<caret>on person = {};
}

@Description {value:"Represents a person"}
@Field {value:"name: name of the person"}
@Field {value:"age: age of the person"}
struct Person {
    string name;
    int age;
}
