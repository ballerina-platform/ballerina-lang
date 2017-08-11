import ballerina.lang.xmls;

function testXMlAttributesMapInvalidUsage() {
    xml x1 = xmls:parse("<root foo1=\"bar1\" foo2=\"bar2\"/>");
    
    map m1 = x1@;
}
