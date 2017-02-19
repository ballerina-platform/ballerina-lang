import ballerina.lang.xmls;

function getString(xml msg, string xPath) (string) {
    return xmls:getString(msg, xPath);
}

function getXML(xml msg, string xPath) (xml) {
    return xmls:getXml(msg, xPath);
}

function setString(xml msg, string xPath, string value) (xml) {
    xmls:set(msg, xPath, value);
    return msg;
}

function setXML(xml msg, string xPath, xml value) (xml) {
    xmls:set(msg, xPath, value);
    return msg;
}

function addElement(xml msg, string xPath, xml value) (xml) {
    xmls:addElement(msg, xPath, value);
    return msg;
}

function addAttribute(xml msg, string xPath, string name, string value) (xml) {
    xmls:addAttribute(msg, xPath, name, value);
    return msg;
}

function remove(xml msg, string xPath) (xml) {
    xmls:remove(msg, xPath);
    return msg;
}

function toString(xml msg) (string) {
    return xmls:toString(msg);
}


function xmlSetString1()(xml) {
     xml payload;
     string doctorName;
     doctorName = "DName1";
     payload = `<CheckAvailability>
                 <doctorName></doctorName>
                 <appointmentDate></appointmentDate>
         </CheckAvailability>`;
     xmls:set(payload, "/CheckAvailability/doctorName", doctorName);
     return payload;
 }

 function xmlSetString2()(xml) {
     xml payload;
     string doctorName;
     doctorName = "DName2";
     payload = `<CheckAvailability>
                 <doctorName>NValue</doctorName>
                 <appointmentDate></appointmentDate>
         </CheckAvailability>`;
     xmls:set(payload, "/CheckAvailability/doctorName/text()", doctorName);
     return payload;
 }