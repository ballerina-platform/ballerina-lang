function testJsonArrayWithUnsupportedtypes() (json) {
    message m = {};
    json j = ["a", "b", "c", m];
    return j;
}