type R record {
    int i;
};

function testXMLAttributeAccessWithNamespaceSyntaxNeg() {
    R r = {i: 0};
    var p = r.b:c;

    map<string> m = {};
    var q = m.b:c;
}

