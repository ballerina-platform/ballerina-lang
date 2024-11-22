function testXmlNegativeIndexedAndFilterStepExtend() {
    string s = "a";
    xml x1 = xml `<item><name>T-shirt</name><price>19.99</price></item>`;
    _ = x1/*[j].<name>;
    _ = x1/*[s].<name>;
    _ = x1/*.<ns:s>;
    _ = x2/*.<s>;
    _ = x2/*.<s>[j];
}

function testXmlMethodCallNegativeStepExtend() returns error? {
    int k = 0;
    string s = "s";

    xml x1 = xml `<item><name>T-shirt</name><price>19.99</price></item>`;

    _ = x1/*.get("0");
    _ = x1/*.get(0, 2);
    _ = x1/*.get(s);

    _ = x1/*.foo();
    _ = x1/*.length();
    _ = x1/*.slice(1, "5");

    _ = x1/*.slice(0).length();
    _ = x1/*.elementChildren().get(0).getChildren();


    _ = x1/<item>.get(r);
    _ = x1/<item>.get(0).getChildren();
    _ = x1/<item>.filter(x => x);

    _ = x1/<item>.map(y => xml:createProcessingInstruction(y.getTarget(), "sort"));
    _ = x1/<item>.forEach(function (xml y) {k = k + 1;});

    _ = x1/**/<item>.get(r);
    _ = x1/**/<item>.get(0).getChildren(); // error at the wrong place + invalid error
    _ = x1/**/<item>.filter(x => x);
    _ = x1/**/<item>.filter(function (xml y) {k = k + 1;});
}
