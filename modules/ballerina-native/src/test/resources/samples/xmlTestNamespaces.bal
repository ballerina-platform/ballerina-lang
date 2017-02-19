import ballerina.lang.xmlutils;

function getString(xml msg, string xPath, map namespaces) (string) {
    return xmlutils:getString(msg, xPath, namespaces);
}

function getXML(xml msg, string xPath, map namespaces) (xml) {
    return xmlutils:getXml(msg, xPath, namespaces);
}

function setString(xml msg, string xPath, string value, map namespaces) (xml) {
    xmlutils:set(msg, xPath, value, namespaces);
    return msg;
}

function setXML(xml msg, string xPath, xml value, map namespaces) (xml) {
    xmlutils:set(msg, xPath, value, namespaces);
    return msg;
}

function addElement(xml msg, string xPath, xml value, map namespaces) (xml) {
    xmlutils:addElement(msg, xPath, value, namespaces);
    return msg;
}

function addAttribute(xml msg, string xPath, string name, string value, map namespaces) (xml) {
    xmlutils:addAttribute(msg, xPath, name, value, namespaces);
    return msg;
}

function remove(xml msg, string xPath, map namespaces) (xml) {
    xmlutils:remove(msg, xPath, namespaces);
    return msg;
}