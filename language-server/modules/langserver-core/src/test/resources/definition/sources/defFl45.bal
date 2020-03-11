type Person record {
    string name;
    int age;
};

function testIterator() {
    Person[] personList = [];
    stream<Person, error> personStream = personList.toStream();
}
