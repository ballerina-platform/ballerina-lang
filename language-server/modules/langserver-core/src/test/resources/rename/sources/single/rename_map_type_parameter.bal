type Person record {|
    readonly int id;
    string name;
|};

function foo() {
    map<Person> m = new;
}
