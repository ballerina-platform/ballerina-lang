type Person record {|
    readonly int id;
    string name;
|};

function foo() {
    table<Person> tHuman = table [{id: 1, name: "Jane"}];
}
