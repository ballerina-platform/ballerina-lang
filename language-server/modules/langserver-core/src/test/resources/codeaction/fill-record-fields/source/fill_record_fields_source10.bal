type School record {
    string name;
    string city;
};

public isolated client class SchoolClient {
    isolated remote function addSchool(School school) {}
}

public function testFunc() {
    SchoolClient school = new();
    school->addSchool({});
}
