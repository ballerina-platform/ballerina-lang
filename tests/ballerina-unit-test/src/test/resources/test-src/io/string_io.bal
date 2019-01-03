import ballerina/io;

function getJson(string content, string encoding) returns json|error {
    io:StringReader reader = new io:StringReader(content, encoding = encoding);
    var readResult = reader.readJson();
    var closeResult = reader.close();
    if (readResult is json) {
        return readResult;
    } else if (readResult is error) {
        return readResult;
    } else {
        error e = error("Unidentified type");
        return e;
    }
}

function getXml(string content, string encoding) returns xml?|error {
    io:StringReader reader = new io:StringReader(content, encoding = encoding);
    var readResult = reader.readXml();
    var closeResult = reader.close();
    if (readResult is xml?) {
        return readResult;
    } else if (readResult is error) {
        return readResult;
    } else {
        error e = error("Unidentified type");
        return e;
    }
}
