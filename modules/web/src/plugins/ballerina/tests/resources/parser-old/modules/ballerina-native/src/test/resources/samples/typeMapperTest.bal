function xmltojson(xml input)(json) {
    json result;
    result, _ = <json>input;
    return result;
}

function jsontoxml(json input)(xml) {
    xml result;
    result, _ = <xml>input;
    return result;
}

function stringtojson(string value)(json) {
    json result;
    result = (json) value;
    return result;
}
