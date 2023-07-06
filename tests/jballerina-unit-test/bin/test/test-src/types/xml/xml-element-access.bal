import ballerina/lang.'xml as xmllib;

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
