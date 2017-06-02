function testBlobParameter(blob b) (blob) {
    blob a;
    a = b;
    return a;
}

function testBlobParameterArray(blob b) (blob) {
    blob [] a = [];
    a[0] = b;
    return a[0];
}
