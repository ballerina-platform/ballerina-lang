type Employee record {|
    int id;
    string name;
|};

function testFrozenAnyArrayElementUpdate() returns error? {
    Employee e1 = {name: "Em", id: 1000};
    anydata[] i1 = [e1];
    anydata[] i2 = i1.cloneReadOnly();
    Employee e2 = check trap <Employee>i2[0];
    e2["name"] = "Zee";
    return ();
}

function testFrozenTupleUpdate() {
    Employee e1 = {name: "Em", id: 1000};
    [int, Employee] t1 = [1, e1];
    [int, Employee] t2 = t1.cloneReadOnly();
    Employee e2 = {name: "Zee", id: 1200};
    t2[1] = e2;
}
