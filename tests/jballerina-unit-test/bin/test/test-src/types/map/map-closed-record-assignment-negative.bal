type Teacher record {|
    int index;
    int age;
|};

function testMapAnyDataClosedRecordAssignmentNegative() returns (anydata) {
    Teacher teacher = {index:1001, age:25};
    map<anydata> m = teacher;
    m["name"] = "Name";
    return m["name"];
}
