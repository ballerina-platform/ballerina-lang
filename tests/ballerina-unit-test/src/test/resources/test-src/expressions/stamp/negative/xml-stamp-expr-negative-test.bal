type BookRecord record {
   string book;
};

type BookObject object {
    string book;
};

function stampXMLToRecord() returns BookRecord {

    xml xmlValue = xml `<book>The Lost World</book>`;

    BookRecord recordValue = xmlValue.stamp(BookRecord);
    return recordValue;
}

function stampJSONToRecord() returns json {

    xml xmlValue = xml `<book>The Lost World</book>`;

    json jsonValue = xmlValue.stamp(json);
    return jsonValue;
}

function stampXMLToObject() returns BookObject {

    xml xmlValue = xml `<book>The Lost World</book>`;

    BookObject objectValue = xmlValue.stamp(BookObject);
    return objectValue;
}

function stampXMLToMap() returns map {

    xml xmlValue = xml `<book>The Lost World</book>`;

    map mapValue = xmlValue.stamp(map);
    return mapValue;
}

function stampXMLToArray() returns BookRecord[] {

    xml xmlValue = xml `<book>The Lost World</book>`;

    BookRecord[] arrayValue = xmlValue.stamp(BookRecord[]);
    return arrayValue;
}

function stampXMLToTuple() returns (string, string) {

    xml xmlValue = xml `<book>The Lost World</book>`;

    (string, string) tupleValue = xmlValue.stamp((string, string));
    return tupleValue;
}
