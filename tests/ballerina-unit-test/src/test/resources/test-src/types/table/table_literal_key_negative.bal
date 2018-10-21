type Person record {
    int id;
    string name;
    int age;
    !...
};

function testTableLiteralDataAndAddWithObject() returns (int) {
    //Object types cannot be included in the literal
    table<Person> t1 = table {
        { key id, keyerror name, age }
    };

    int count = t1.count();
    return count;
}