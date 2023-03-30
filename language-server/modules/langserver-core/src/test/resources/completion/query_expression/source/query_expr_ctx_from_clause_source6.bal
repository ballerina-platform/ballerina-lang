type Student record {|
    readonly int id;
    string name;
|};

function testStreamType() {
    map<Student> students = {};
    from  in students
}
