import ballerina/lang.xmls;

function getString(xml msg, string xPath, map namespaces) (string) {
    return xmls:getStringWithNamespace(msg, xPath, namespaces);
}

function getXML(xml msg, string xPath, map namespaces) (xml) {
    return xmls:getXmlWithNamespace(msg, xPath, namespaces);
}

function setString(xml msg, string xPath, string value, map namespaces) (xml) {
    xmls:setStringWithNamespace(msg, xPath, value, namespaces);
    return msg;
}

function setXML(xml msg, string xPath, xml value, map namespaces) (xml) {
    xmls:setXmlWithNamespace(msg, xPath, value, namespaces);
    return msg;
}

function addElement(xml msg, string xPath, xml value, map namespaces) (xml) {
    xmls:addElementWithNamespace(msg, xPath, value, namespaces);
    return msg;
}

function addAttribute(xml msg, string xPath, string name, string value, map namespaces) (xml) {
    xmls:addAttributeWithNamespace(msg, xPath, name, value, namespaces);
    return msg;
}

function remove(xml msg, string xPath, map namespaces) (xml) {
    xmls:removeWithNamespace(msg, xPath, namespaces);
    return msg;
}