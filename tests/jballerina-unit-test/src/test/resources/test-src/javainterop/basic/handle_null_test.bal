import ballerinax/java;

function testCreateNullHandle() returns handle {
    handle nullHandle = java:createNull();
    return nullHandle;
}

function testIsNull(handle value) returns boolean {
    return java:isNull(value);
}
