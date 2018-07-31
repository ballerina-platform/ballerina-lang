public type Person record {
   string name;
};

function testAmbiguousAssignment() {
    Person|error x = {name:"John", id:12}; // Compile error since we can not infer type from record literal
}
