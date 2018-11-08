type BookRecord record {
   string book;
};

type BookObject object {
    string book;
};

function sealXMLToRecord() returns BookRecord {

    xml xmlValue = xml `<book>The Lost World</book>`;

    BookRecord recordValue = xmlValue.seal(BookRecord);
    return recordValue;
}

function sealJSONToRecord() returns json {

    xml xmlValue = xml `<book>The Lost World</book>`;

    json jsonValue = xmlValue.seal(json);
    return jsonValue;
}

function sealXMLToObject() returns BookObject {

    xml xmlValue = xml `<book>The Lost World</book>`;

    BookObject objectValue = xmlValue.seal(BookObject);
    return objectValue;
}

function sealXMLToMap() returns map {

    xml xmlValue = xml `<book>The Lost World</book>`;

    map mapValue = xmlValue.seal(map);
    return mapValue;
}

function sealXMLToArray() returns BookRecord[] {

    xml xmlValue = xml `<book>The Lost World</book>`;

    BookRecord[] arrayValue = xmlValue.seal(BookRecord[]);
    return arrayValue;
}

function sealXMLToTuple() returns (string, string) {

    xml xmlValue = xml `<book>The Lost World</book>`;

    (string, string) tupleValue = xmlValue.seal((string, string));
    return tupleValue;
}
