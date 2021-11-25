function testXMLNavigationOnSingleElement() {
    xml x1 = xml `<root><child attr="attr-val"></child></root>`;
    xml _ = x1/<child>.clone();
    xml _ = x1/*.clone();
    xml _ = x1/<*>.clone();
    xml _ = x1/**/<child>.clone();
    xml _ = x1/<child>[0].clone();
    xml _ = x1/*[0];
    xml _ = x1/**/<child>[0];
}
