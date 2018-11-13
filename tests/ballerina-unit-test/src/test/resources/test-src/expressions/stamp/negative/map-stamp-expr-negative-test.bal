
type IntObject object{
    string firstName;
    string lastName;
};

//----------------------------Map Stamp Negative Test Cases-------------------------------------------------------------

function stampMapToXML() returns xml {
    map<any> m = { "firstName": "mohan", "lastName": "raj" };
    xml xmlValue = xml.stamp(m);

    return xmlValue;
}

function stampMapToArray() returns string[] {
    map<anydata> m = { "firstName": "mohan", "lastName": "raj" };
    string[] arrayValue = string[].stamp(m);

    return arrayValue;
}

function stampMapToTuple() returns (string,string) {
    map<anydata> m = { "firstName": "mohan", "lastName": "raj" };
    (string,string) tupleValue = (string,string).stamp(m);

    return tupleValue;
}

function stampMapToObject() returns IntObject {
    map<anydata> m = { "firstName": "mohan", "lastName": "raj" };
    IntObject objectValue = IntObject.stamp(m);

    return objectValue;
}
