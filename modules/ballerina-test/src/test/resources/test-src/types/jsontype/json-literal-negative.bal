function testJsonArrayWithUnsupportedtypes() (json) {
    datatable dt;
    json j = ["a", "b", "c", dt];
    return j;
}

function testJsonInitWithUnsupportedtypes() (json) {
    datatable dt;
    json j = {"name":"Supun", "value":dt};
    return j;
}
