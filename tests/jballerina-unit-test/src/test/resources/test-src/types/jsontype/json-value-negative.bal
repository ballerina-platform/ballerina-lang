type DummyType record {
   int id;
};

function testJsonArrayWithUnsupportedtypes() returns (json) {
    table<DummyType> dt = table{};
    json j = ["a", "b", "c", dt];
    return j;
}

function testJsonInitWithUnsupportedtypes() returns (json) {
    table<DummyType> dt = table{};
    json j = { "name": "Supun", "value": dt };
    return j;
}
