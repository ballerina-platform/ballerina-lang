import ballerina.lang.converters;

typeconvertor jsonToXmlConvertor(json input) (xml) {
    xml result;
    result = (xml)input;
    return result;
}