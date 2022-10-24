type RecordType record {
    json|error fieldName;
};

function testFunction(RecordType rec) {
    json|error var1 = rec.fieldName;
    string var2 = var1 is json? var1.toString() : "";
}
