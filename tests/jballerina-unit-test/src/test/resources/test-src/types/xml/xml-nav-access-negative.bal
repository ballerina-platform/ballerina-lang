function testXMLNavigationOnSingleElement() returns [xml, xml, xml, xml, xml] {
    xml x1 = xml `<root><child attr="attr-val"></child></root>`;
    xml x2 = x1/<child>.clone();
    xml x3 = x1/*.clone();
    xml x4 = x1/<*>.clone();
    xml x5 = x1/**/<child>.clone();
    xml x6 = x1/<child>[0].clone();

    return [x2, x3, x4, x5, x6];
}
