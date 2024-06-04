function testXmlNegativeIndexedAndFilterStepExtend() {
    string s = "a";
    xml x1 = xml `<item><name>T-shirt</name><price>19.99</price></item>`;
    _ = x1/*[j].<name>;
    _ = x1/*[s].<name>;
    _ = x1/*.<ns:s>;
    _ = x2/*.<s>;
    _ = x2/*.<s>[j];
}
