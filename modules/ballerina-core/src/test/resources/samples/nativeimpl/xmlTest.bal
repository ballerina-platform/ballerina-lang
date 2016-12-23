function getString(xml msg, string xPath) (string) {
    return ballerina.lang.xml:getString(msg, xPath);
}

function getXML(xml msg, string xPath) (string) {
    return ballerina.lang.xml:getXml(msg, xPath);
}

function setString(xml msg, string xPath, string value) (xml) {
    ballerina.lang.xml:set(msg, xPath, value);
    return msg;
}

function setXML(xml msg, string xPath, xml value) (xml) {
    ballerina.lang.xml:set(msg, xPath, value);
    return msg;
}

function addElement(xml msg, string xPath, xml value) (xml) {
    ballerina.lang.xml:addElement(msg, xPath, value);
    return msg;
}

function addAttribute(xml msg, string xPath, string name, string value) (xml) {
    ballerina.lang.xml:addAttribute(msg, xPath, name, value);
    return msg;
}

function remove(xml msg, string xPath) (string) {
    ballerina.lang.xml:remove(msg, xPath);
    return msg;
}