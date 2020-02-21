type Person record {
    string name;
    Person employer?;
};

function testRecordFieldAccess1() {
    Person bob = {
        name: "Bob"
    };
    string name = bob.
}
