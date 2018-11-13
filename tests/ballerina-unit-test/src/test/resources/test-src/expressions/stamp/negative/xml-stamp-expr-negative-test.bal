type BookRecord record {
   string book;
};

type BookObject object {
    string book;
};

function stampXMLToRecord() returns BookRecord {

    xml xmlValue = xml `<book>The Lost World</book>`;

    BookRecord recordValue = BookRecord.stamp(xmlValue);
    return recordValue;
}

function stampJSONToRecord() returns json {

    xml xmlValue = xml `<book>The Lost World</book>`;

    json jsonValue = json.stamp(xmlValue);
    return jsonValue;
}

function stampXMLToObject() returns BookObject {

    xml xmlValue = xml `<book>The Lost World</book>`;

    BookObject objectValue = BookObject.stamp(xmlValue);
    return objectValue;
}

function stampXMLToMap() returns map {

    xml xmlValue = xml `<book>The Lost World</book>`;

    map mapValue = map.stamp(xmlValue);
    return mapValue;
}

function stampXMLToArray() returns BookRecord[] {

    xml xmlValue = xml `<book>The Lost World</book>`;

    BookRecord[] arrayValue = BookRecord[].stamp(xmlValue);
    return arrayValue;
}

function stampXMLToTuple() returns (string, string) {

    xml xmlValue = xml `<book>The Lost World</book>`;

    (string, string) tupleValue = (string, string).stamp(xmlValue);
    return tupleValue;
}
