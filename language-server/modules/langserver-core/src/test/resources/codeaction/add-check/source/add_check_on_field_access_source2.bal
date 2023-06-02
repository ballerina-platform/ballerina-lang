type Data record {|
    json|error field1;
|};


function toString(Data data) returns error? {
    json|error field1 = data.field1;
    string str = (field1).toString();
}
