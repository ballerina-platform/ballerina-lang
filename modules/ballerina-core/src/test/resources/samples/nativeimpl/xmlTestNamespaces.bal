import ballerina.lang.xml;

function getString(xml msg, string xPath, map namespaces) (string) {
    return xml:getString(msg, xPath, namespaces);
}

function getXML(xml msg, string xPath, map namespaces) (xml) {
    return xml:getXml(msg, xPath, namespaces);
}

function setString(xml msg, string xPath, string value, map namespaces) (xml) {
    xml:set(msg, xPath, value, namespaces);
    return msg;
}

function setXML(xml msg, string xPath, xml value, map namespaces) (xml) {
    xml:set(msg, xPath, value, namespaces);
    return msg;
}

function addElement(xml msg, string xPath, xml value, map namespaces) (xml) {
    xml:addElement(msg, xPath, value, namespaces);
    return msg;
}

function addAttribute(xml msg, string xPath, string name, string value, map namespaces) (xml) {
    xml:addAttribute(msg, xPath, name, value, namespaces);
    return msg;
}

function remove(xml msg, string xPath, map namespaces) (xml) {
    xml:remove(msg, xPath, namespaces);
    return msg;
}