
type IntObject object{
    string firstName;
    string lastName;
};

//----------------------------Map Stamp Negative Test Cases-------------------------------------------------------------

function stampMapToXML() returns xml {
    map<any> m = { "firstName": "mohan", "lastName": "raj" };
    xml xmlValue = m.stamp(xml);

    return xmlValue;
}

function stampMapToArray() returns string[] {
    map<any> m = { "firstName": "mohan", "lastName": "raj" };
    string[] arrayValue = m.stamp(string[]);

    return arrayValue;
}

function stampMapToTuple() returns (string,string) {
    map<any> m = { "firstName": "mohan", "lastName": "raj" };
    (string,string) tupleValue = m.stamp((string,string));

    return tupleValue;
}

function stampMapToObject() returns IntObject {
    map<any> m = { "firstName": "mohan", "lastName": "raj" };
    IntObject objectValue = m.stamp(IntObject);

    return objectValue;
}
