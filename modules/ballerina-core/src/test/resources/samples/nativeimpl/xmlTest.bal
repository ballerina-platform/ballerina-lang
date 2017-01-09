import ballerina.lang.xml;

function getString(xml msg, string xPath) (string) {
    return xml:getString(msg, xPath);
}

function getXML(xml msg, string xPath, map namespaces) (string) {
    return xml:getXml(msg, xPath, namespaces);
}

function setString(xml msg, string xPath, string value) (xml) {
    xml:set(msg, xPath, value);
    return msg;
}

function setXML(xml msg, string xPath, xml value) (xml) {
    xml:set(msg, xPath, value);
    return msg;
}

function addElement(xml msg, string xPath, xml value) (xml) {
    xml:addElement(msg, xPath, value);
    return msg;
}

function addAttribute(xml msg, string xPath, string name, string value) (xml) {
    xml:addAttribute(msg, xPath, name, value);
    return msg;
}

function remove(xml msg, string xPath) (string) {
    xml:remove(msg, xPath);
    return msg;
}