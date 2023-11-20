type Person record {|
    readonly int id;
    string name;
|};

function foo() {
    Person p = { id: 1, name: "foo" };
    stream<Person> s = new;
}
