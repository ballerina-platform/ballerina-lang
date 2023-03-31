type Student record {|
    readonly int id;
    string name;
|};

function testStreamType() {
    stream<Student> students = new;
    from  in students
}
