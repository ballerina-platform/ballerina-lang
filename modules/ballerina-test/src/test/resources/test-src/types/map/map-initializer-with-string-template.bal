function testMapInitWithStringTemplateAsKey() (map) {
    string prefix = "first";
    map m = {string `{{prefix}}name` : "John"};
    return m;
}
