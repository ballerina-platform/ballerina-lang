blob glbVarBlob;

type BinaryData {
    blob blobField;
};

function testBlobParameter(blob b) returns (blob) {
    blob a;
    a = b;
    return a;
}

function testGlobalVariable(blob b) returns (blob) {
    glbVarBlob = b;
    return glbVarBlob;
}

function testBlobParameterArray(blob b1, blob b2) returns (blob, blob) {
    blob [] a = [];
    a[0] = b1;
    a[1] = b2;
    return (a[0], a[1]);
}

function testBlobField(blob b) returns (blob) {
    BinaryData bytes = {blobField:b};
    return bytes.blobField;
}
