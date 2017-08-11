function xmlFormat() {
    xmlElement e1;
    xmlElement e2;
    string s1;
    xmlElement e3;
    xmlElement e4;
    string s3;
    //following samples show XML literal templates
    e1 = `<hello>world</hello>`;
    e2 = `<nested>$e1</nested>`;

    s1 = "say hello";
    e3 = `<n1:greet xmlns:n1="http://example.com/greet">$s1</n1:greet>`;

    s2 = xmls:get(e3, "/x:greet", {"x":"http://example.com/greet"});
    //assert(s1 == s2);

    e4 = `<city zip="SA">sample</city>`;
    s3 = xmls:get(e4, "@zip", null);
    log:info("expecting 'SA', got '" + s3 + "'");

    xmls:set(e1, "/hello", null, "earth");
    //assert(e1 == `<hello>earth</hello>`);

}
