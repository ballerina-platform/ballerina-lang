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


function backtickStringVariableAccessJSON(string variable) (json) {
    json backTickMessage;
    backTickMessage = `{"name":${variable}}`;
    return backTickMessage;
}

function backtickIntegerVariableAccessJSON(int variable) (json) {
    json backTickMessage;
    backTickMessage = `{"age":${variable}}`;
    return backTickMessage;
}

function backticJSONEnrichFullJSON(int variable) (json) {
    json msg;
    json backTickMessage;
    msg = `{"name":"John"}`;
    backTickMessage = `${msg}`;
    return backTickMessage;
}

function backticJSONMultipleVariables(string fname, string lname) (json) {
    json msg;
    json backTickMessage;
    msg = `{"name":{"first_name":${fname}, "last_name":${lname}}}`;
    backTickMessage = `${msg}`;
    return backTickMessage;
}

function backticJSONParts(string firstSection, string secondSection) (json) {
    json backTickMessage;
    backTickMessage = `${firstSection}${secondSection}`;
    return backTickMessage;
}