function xmltojson(xml input)(json) {
    json result;
    result = <json>input;
    return result;
}

function jsontoxml(json input)(xml) {
    xml result;
    result = <xml>input;
    return result;
}

function stringtojson(string value)(json) {
    json result;
    result = <json> value;
    return result;
}

function jsontostring(json value)(string) {
    string result;
    result = (string)value;
    return result;
}