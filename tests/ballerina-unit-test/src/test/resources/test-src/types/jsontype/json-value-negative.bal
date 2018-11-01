function testJsonArrayWithUnsupportedtypes() returns (json) {
    table dt;
    json j = ["a", "b", "c", dt];
    return j;
}

function testJsonInitWithUnsupportedtypes() returns (json) {
    table dt;
    json j = {"name":"Supun", "value":dt};
    return j;
}
