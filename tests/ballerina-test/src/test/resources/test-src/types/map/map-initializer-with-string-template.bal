function testMapInitWithStringTemplateAsKey() returns (map) {
    string prefix = "first";
    map m = {string `{{prefix}}name` : "John"};
    return m;
}
