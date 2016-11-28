function xmlFormat() {

    xmlElement e1 = `<hello>world</hello>`;
    xmlElement e2 = `<nested>$e1</nested>`;

    string s1 = "say hello";
    xmlElement e3 = `<n1:greet xmlns:n1="http://example.com/greet">$s1</n1:greet>`;

    string s2 = xml.get(e3, "/x:greet", {"x":"http://example.com/greet"});
    //assert(s1 == s2);

    xmlElement e4 = `<city zip="SA">sample</city>`;
    string s3 = xml.get(e4, "@zip", null);
    log.info("expecting 'SA', got '" + s3 + "'");

    xml.set(e1, "/hello", null, "earth");
    //assert(e1 == `<hello>earth</hello>`);

}
