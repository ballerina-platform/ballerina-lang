 import ballerina/lang.'xml as xmllib;

function testXMLNavigationOnSingleElement() returns [xml, xml, xml, xml, xml] {
    xml x1 = xml `<root><child attr="attr-val"></child></root>`;
    xml x2 = x1/<child>;
    xml x3 = x1/*; // all children of each element of x1
    xml x4 = x1/<*>; // all elements children of each element of x1
    xml x5 = x1/**/<child>; // descendants
    xml x6 = x1/<child>[0]; // first child of each element in x1 that match ns:child

    return [x2, x3, x4, x5, x6];
}

function testXMLNavigationOnSingleElementWithNamespaces() returns [xml, xml, xml, xml, xml] {
    xmlns "foo" as ns;
    xmlns "bar" as k;
    xml x1 = xml `<ns:root><ns:child></ns:child></ns:root>`;
    xml x2 = x1/<ns:child>;
    xml x3 = x1/*;
    xml x4 = x1/<*>;
    xml x5 = x1/**/<ns:child>;
    xml x6 = x1/<ns:child>[0];

    return [x2, x3, x4, x5, x6];
}


function testXMLNavigationOnSingleElementReferToDefaultNS()
        returns [xml, xml, xml, xml, xml, xml, int, xml] {
    xmlns "foo";
    xmlns "bar" as k;
    xml x1 = xml `<root><child></child></root>`;
    xml x2 = x1/<child>;
    xml x3 = x1/*;
    xml x4 = x1/<*>;
    xml x5 = x1/**/<child>;
    xml x6 = x1/<child>[0];
    xml x7 = (x1/*)[0];
    xml x8 = (x1/*)[1];
    xml x9 = (x1/**/<child>)[0];

    return [x2, x3, x4, x5, x6, x7, x8.length(), x9];
}


function testXMLNavigationOnSingleElementReferToDefaultNSViaPrefix() returns [xml, xml, xml, xml, xml] {
    xmlns "foo";
    xmlns "bar" as k;
    xmlns "foo" as ns;
    xml x1 = xml `<root><child></child></root>`;
    xml x2 = x1/<ns:child>;
    xml x3 = x1/*;
    xml x4 = x1/<*>;
    xml x5 = x1/**/<ns:child>;
    xml x6 = x1/<ns:child>[0];

    return [x2, x3, x4, x5, x6];
}

function testXMLNavigationOnSequence() returns [xml, xml, xml, xml, xml] {
    xml x1 = xmllib:concat(xml `<root><child>A</child></root>`,
                        xml `<root><child>B</child></root>`,
                        xml `<item><child>C</child><it-child>D</it-child>TEXT</item>`);
    xml x2 = x1/<child>;
    xml x3 = x1/*;
    xml x4 = x1/<*>;
    xml x5 = x1/**/<child>;
    xml x6 = x1/<child>[0];
    return [x2, x3, x4, x5, x6];
}

function testXMLNavigationOnSequenceWithNamespaces() returns [xml, xml, xml, xml, xml] {
    xmlns "foo";
    xmlns "bar" as k;
    xmlns "foo" as ns;
    xml x1 = xmllib:concat(xml `<root><child>A</child></root>`,
                        xml `<root><ns:child>B</ns:child></root>`,
                        xml `<item><k:child>C</k:child><it-child>D</it-child>TEXT</item>`);
    xml x2 = x1/<child>;
    xml x3 = x1/*;
    xml x4 = x1/<*>;
    xml x5 = x1/**/<ns:child>;
    xml x6 = x1/<child>[0];
    return [x2, x3, x4, x5, x6];
}

function testXMLNavigationOnSequenceWithNamespacesAndMultipleFilters() returns [xml, xml, xml, xml, xml] {
    xmlns "foo";
    xmlns "bar" as k;
    xmlns "foo" as ns;
    xml x1 = xmllib:concat(xml `<root><child>A</child></root>`,
                        xml `<root><ns:child>B</ns:child></root>`,
                        xml `<item><k:child>C</k:child><child2>D</child2>TEXT</item>`);
    xml x2 = x1/<child|child2>;
    xml x3 = x1/*;
    xml x4 = x1/<*>;
    xml x5 = x1/**/<ns:child|k:child|child2>;
    xml x6 = x1/<child|child2>[0];
    return [x2, x3, x4, x5, x6];
}

function testXMLElementAccessNavigationAccessComposition() returns [xml, xml, xml, xml, xml, xml, xml] {
    xml x = xml `<root>
        <person><name><fname>Kamal</fname><lname>Gunae</lname></name></person>
        <person><name><fname>Nimal</fname><lname>Jayee</lname></name></person>
        <person><name><fname>Sunil</fname><lname>Kumarae</lname></name></person>
        </root>`;

    xml c = x/**/<name>/<lname>;
    xml d = x/**/<name>/<lname|fname>;
    xml e = x/**/<name>/<lname|fname>.<lname>;
    xml f = x/**/<name>/<lname|fname>.<fname>;
    xml g = x/**/<name>/<lname|fname>/*;
    xml h = x/**/<fname>;
    xml i = x/<person>/**/<fname>;
    return [c, d, e, f, g, h, i];
}
