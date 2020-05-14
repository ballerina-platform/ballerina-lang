function testMapInitWithStringTemplateAsKey() returns (map<any>) {
    string prefix = "first";
    map<any> m = { [string `${prefix}name`]: "John" };
    return m;
}
