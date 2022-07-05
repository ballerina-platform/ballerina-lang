type Employee record {|
    string name;
|};

function testFunction() {
    Employee emp = {name: "John"}
    int i = getInt(emp.name);
}
