type TestRecordType record {
    string name;
    int age;
};

public function TestFunc(string[] names, int age) returns string[]|error {
    TestRecordType[] rec = [];
    foreach var item in names {
        TestRecordType[] result = [{name: "John", age: 20}];
        rec.push(...result);
    }
}
