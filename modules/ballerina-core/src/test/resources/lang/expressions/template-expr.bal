import ballerina.lang.xml;
import ballerina.lang.json;

function backtickXMLTest (string name)(xml) {
    xml msg;
    msg = `<name>John</name>`;
    return msg;
}

function backtickJSONTest (string name)(json) {
    json msg;
    msg = `{"name":"John"}`;
    return msg;
}

function backtickVariableAccessXML(string variable) (xml) {
    xml backTickMessage;
    backTickMessage = `<name>${variable}</name>`;
    return backTickMessage;
}


function backtickVariableAccessJSON(string variable) (json) {
    json backTickMessage;
    backTickMessage = `{"name":${variable}}`;
    return backTickMessage;
}