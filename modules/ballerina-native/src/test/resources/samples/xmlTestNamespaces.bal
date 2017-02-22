import ballerina.lang.xmls;

function getString(xml msg, string xPath, map namespaces) (string) {
    return xmls:getString(msg, xPath, namespaces);
}

function getXML(xml msg, string xPath, map namespaces) (xml) {
    return xmls:getXml(msg, xPath, namespaces);
}

function setString(xml msg, string xPath, string value, map namespaces) (xml) {
    xmls:set(msg, xPath, value, namespaces);
    return msg;
}

function setXML(xml msg, string xPath, xml value, map namespaces) (xml) {
    xmls:set(msg, xPath, value, namespaces);
    return msg;
}

function addElement(xml msg, string xPath, xml value, map namespaces) (xml) {
    xmls:addElement(msg, xPath, value, namespaces);
    return msg;
}

function addAttribute(xml msg, string xPath, string name, string value, map namespaces) (xml) {
    xmls:addAttribute(msg, xPath, name, value, namespaces);
    return msg;
}

function remove(xml msg, string xPath, map namespaces) (xml) {
    xmls:remove(msg, xPath, namespaces);
    return msg;
}