function testJsonInitWithUnsupportedtypes() (json) {
    message m = {};
    json j = {"name":"Supun", "value":m};
    return j;
}