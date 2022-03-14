type Person record {
    readonly string name;
    int age;
};

type PersonTable table<Person> key(name);

public function main() {
    PersonTable t1 = table [
        {name: "A", age: 2},
        {n}
    ];
}
