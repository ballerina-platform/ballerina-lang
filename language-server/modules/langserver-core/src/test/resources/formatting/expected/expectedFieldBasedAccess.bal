type TestRecord record {
    int id = 0;
    string name;
};

function name() {
    TestRecord rec = {
        id: 0,
        name: "marcus"
    };

    string nameTest = rec.name;
}
