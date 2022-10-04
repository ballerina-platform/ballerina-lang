type Student record {|
    readonly int id;
    string name;
|};

function testTableType() {
    table<Student> key(id) students = table [
        { id: 1, name: "John" }
    ];
    from  in students
}
