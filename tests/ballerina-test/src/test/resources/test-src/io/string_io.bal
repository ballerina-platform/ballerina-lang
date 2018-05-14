import ballerina/io;

function getJson(string content, string encoding) returns json {
    io:StringReader reader = new io:StringReader(content, encoding = encoding);
    json result = check reader.readJson();
    var closeResult = reader.close();
    return result;
}

function getXml(string content, string encoding) returns xml? {
    io:StringReader reader = new io:StringReader(content, encoding = encoding);
    xml? result = check reader.readXml();
    var closeResult = reader.close();
    return result;
}
