import ballerina.lang.system;
import ballerina.lang.xml;

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