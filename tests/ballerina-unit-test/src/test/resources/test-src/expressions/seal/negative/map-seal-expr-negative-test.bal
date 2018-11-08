
type IntObject object{
    string firstName;
    string lastName;
};

//----------------------------Map Seal Negative Test Cases-------------------------------------------------------------

function sealMapToXML() returns xml {
    map<any> m = { "firstName": "mohan", "lastName": "raj" };
    xml xmlValue = m.seal(xml);

    return xmlValue;
}

function sealMapToArray() returns string[] {
    map<any> m = { "firstName": "mohan", "lastName": "raj" };
    string[] arrayValue = m.seal(string[]);

    return arrayValue;
}

function sealMapToTuple() returns (string,string) {
    map<any> m = { "firstName": "mohan", "lastName": "raj" };
    (string,string) tupleValue = m.seal((string,string));

    return tupleValue;
}

function sealMapToObject() returns IntObject {
    map<any> m = { "firstName": "mohan", "lastName": "raj" };
    IntObject objectValue = m.seal(IntObject);

    return objectValue;
}
