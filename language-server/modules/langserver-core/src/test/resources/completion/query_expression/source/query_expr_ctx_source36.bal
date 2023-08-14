type Student record {|
    int id;
    string name;
|};

function foo() {
    Student[] students = [];
    _ = from var {id, name} in students
            where name == ""
            where id is int 

    string s = "";
}
