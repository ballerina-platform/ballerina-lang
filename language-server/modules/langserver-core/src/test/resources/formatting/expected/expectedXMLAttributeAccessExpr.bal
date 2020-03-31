function testAddAttributeWithString() returns (xml) {
    var x1 = xml `<root xmlns:ns4="http://sample.com/wso2/f"></root>`;
    x1@["foo1"] = "bar1";

    // with a new namespaceUri
    x1
    @
    [
        "{http://sample.com/wso2/e}foo2"
    ] = "bar2"
    ;

    // with an existing namespaceUri
    x1@["{http://sample.com/wso2/f}foo3"] = "bar3"
    ;

    return x1;
}

function testAddAttributeWithQName() returns (xml) {
    var x1 = xml `<root xmlns:ns3="http://sample.com/wso2/f"></root>`;

    x1@[ns0:foo1] = "bar1";
    return x1;
}

function testAddAttributeWithQName1() returns (xml) {
    var x1 = xml `<root xmlns:ns3="http://sample.com/wso2/f"></root>`;

    x1
    @
    [
        ns0
        :
        foo1
    ]
    =
        "bar1";
    return x1;
}

function testAddAttributeWithDiffQName_1() returns (xml) {
    xml x1 = xml `<root xmlns:ns3="http://sample.com/wso2/f" xmlns:ns4="http://sample.com/wso2/f/" xmlns:ns5="http://sample.com/wso2/f/" ns4:diff="yes"></root>`;
    //same uri, non existant prefix
    xmlns "http://sample.com/wso2/f" as pre;
    x1@[pre:foo1] = "bar1";
    return x1;
}

function testAddAttributeWithDiffQName_2() returns (xml) {
    xml x1 =
        xml `<root
        xmlns:ns3=
        "http://sample.com/wso2/f"
        xmlns:ns4
        ="http://sample.com/wso2/f/"
        xmlns
        :
        ns5
        =
        "http://sample.com/wso2/f/"
        ns4:diff="yes"></root>`
    ;
    //same uri, non existant prefix
    xmlns "http://sample.com/wso2/f" as pre;
    x1@[pre:foo1] = "bar1";
    return x1;
}
