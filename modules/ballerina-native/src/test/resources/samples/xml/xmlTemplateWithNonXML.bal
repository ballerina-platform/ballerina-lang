function xmlTemplateWithNonXML() (xml) {
    map m = {};
    xml x1\ = xml `<root xmlns="http://default/namespace">{{m}}</root>`;
    return x;
}
