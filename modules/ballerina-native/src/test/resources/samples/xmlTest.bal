import ballerina.lang.xml;

function getString(xml msg, string xPath) (string) {
    return xml:getString(msg, xPath);
}

function getXML(xml msg, string xPath) (xml) {
    return xml:getXml(msg, xPath);
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

function remove(xml msg, string xPath) (xml) {
    xml:remove(msg, xPath);
    return msg;
}

function toString(xml msg) (string) {
    return xml:toString(msg);
}


function xmlSetString1()(xml) {
     xml payload;
     string doctorName;
     doctorName = "DName1";
     payload = `<CheckAvailability>
                 <doctorName></doctorName>
                 <appointmentDate></appointmentDate>
         </CheckAvailability>`;
     xml:set(payload, "/CheckAvailability/doctorName", doctorName);
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
     xml:set(payload, "/CheckAvailability/doctorName/text()", doctorName);
     return payload;
 }