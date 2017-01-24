import ballerina.lang.converters;

function xmltojson(xml input)(json) {
    json result;
    result = (json)input;
    return result;
}

function jsontoxml(json input)(xml) {
    xml result;
    result = (xml)input;
    return result;
}
