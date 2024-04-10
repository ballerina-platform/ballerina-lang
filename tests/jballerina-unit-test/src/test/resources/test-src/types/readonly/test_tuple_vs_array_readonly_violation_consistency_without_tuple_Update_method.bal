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
