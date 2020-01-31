import ballerina/lang.'xml as xmllib;

function testXMLAccessWithIndex() returns [xml, xml, xml] {
    xml x1 = xml `<root><!-- comment node--><name>supun</name><city>colombo</city></root>`;
    xml x2 = x1[0].*;

    return [x1[0], x2[0], x2[1]];
}

function testXMLAccessWithOutOfIndex() returns (xml) {
    xml x1 = xml `<root><!-- comment node--><name>supun</name><city>colombo</city></root>`;
    return x1[1];
}

function testXMLSequenceAccessWithOutOfIndex() returns (xml) {
    xml x1 = xml `<root><!-- comment node--><name>supun</name><city>colombo</city></root>`;
    xml x2 = x1[0].*;

    return x2[5];
}

function testLengthOfXMLSequence() returns [int, int, int, int] {
    xml x1 = xml `<root><!-- comment node--><name>supun</name><city>colombo</city></root>`;
    xml x2 = x1[0].*;

    xml[] x3 = [x1, x2];
    return [x1.length(), x2.length(), x2[2].length(), x3.length()];
}

// disabling as x3.* operator is not in new xml proposal.
function testFieldBasedAccess() returns [xml, xml, xml, xml, xml, xml] {
    xml x1 = xml `<name1><fname><foo>1</foo><bar>2</bar></fname><lname1><foo>3</foo><bar>4</bar></lname1></name1>`;
    xml x2 = xml `<name2><fname><foo>5</foo><bar>6</bar></fname><lname2><foo>7</foo><bar>8</bar></lname2></name2>`;
    xml x3 = x1 + x2 + xml `<foo>apple</foo>`;

    xml x4 = x3.*;
    xml x5 = x1["fname"];
    xml x6 = x3.fname.foo[1];
    xml x7 = x3.fname[1].foo;
    xml x8 = x3.*.bar[1];
    xml x9 = x3.*.*;

    return [x4, x5, x6, x7, x8, x9];
}

//function testFieldBasedAccessWithNamespaces() returns [xml, xml, xml, xml] {
//    xmlns "http://test.com/default";
//    xmlns "http://test.com" as ns0;
//
//    xml x1 = xml `<ns0:name><ns0:fname>John</ns0:fname><lname1>Doe</lname1></ns0:name>`;
//    xml x2 = xml `<name2><fname>Jane</fname><lname2><foo>7</foo><bar>8</bar></lname2></name2>`;
//    xml x3 = x1 + x2 + xml `<foo>apple</foo>`;
//
//    xml x4 = x1[ns0:fname];
//    xml x5 = x1["{http://test.com}fname"];
//    xml x6 = x1.'ns0\:fname;
//    xml x7 = x1.'\{http\:\/\/test\.com\}fname;
//
//    return [x4, x5, x6, x7];
//}

// todo: move this to own test class (TestXMLElementAccess)
function testXMLElementAccessOnSingleElementXML() returns [xml, xml, xml, xml, xml, xml] {
    xmlns "foo" as ns;
    xmlns "bar" as k;
    xml x1 = xml `<ns:root></ns:root>`;
    xml x2 = x1.<*>; // get all elements
    xml x3 = x1.<ns:*>;
    xml x4 = x1.<ns:root>;
    xml x5 = x1.<ns:other>;
    xml x6 = x1.<other>;
    xml x7 = x1.<k:*>;
    return [x2, x3, x4, x5, x6, x7];
}

function testXMLElementAccessOnXMLSequence() returns [xml, xml, xml, xml, xml, xml] {
    xmlns "foo" as ns;
    xmlns "bar" as k;
    xml x1 = xmllib:concat(xml `<ns:root></ns:root>`, xml `<k:root></k:root>`, xml `<k:item></k:item>`);
    xml x2 = x1.<*>;
    xml x3 = x1.<ns:*>;
    xml x4 = x1.<ns:root>;
    xml x5 = x1.<ns:other>;
    xml x6 = x1.<other>;
    xml x7 = x1.<k:*>;
    return [x2, x3, x4, x5, x6, x7];
}

function testXMLElementAccessMultipleFilters() returns [xml, xml, xml, xml] {
    xmlns "foo" as ns;
    xmlns "bar" as k;
    xml x1 = xmllib:concat(xml `<ns:root></ns:root>`, xml `<k:root></k:root>`, xml `<k:item></k:item>`);
    xml x2 = x1.<ns:*|*>;
    xml x3 = x1.<ns:*|k:*>;
    xml x4 = x1.<ns:root|k:root>;
    xml x5 = x1.<ns:other|k:item>;
    return [x2, x3, x4, x5];
}
