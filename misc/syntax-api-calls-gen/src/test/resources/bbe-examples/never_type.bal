type Person record {
    string name;
    int age;
};

type Student record {
    string name;
    // An optional field can be of type `never`, but a value can never be assigned to such a field.
    never gender?;
};

// This function never returns. So we have `never` as the return type.
function somefunction() returns never {
    panic error("Invalid");
}

public function main() {
    // This is an example of using `never` as the type parameter in `xml`.
    // `xml<never>` describes the `xml` type that has no constituents,
    // i.e., the empty `xml` value.
    xml<never> xmlValue = <xml<never>> 'xml:concat();

    // You can define a mapping value with `never` as its constraint.
    // But you can never add members to this map.
    map<never> someMap = {};

    // You can specify a key-less table with the `never` type as the key constraint.
    table<Person> key<never> personTable = table [
        {name: "John", age: 23},
        {name: "Paul", age: 25}
    ];
}
