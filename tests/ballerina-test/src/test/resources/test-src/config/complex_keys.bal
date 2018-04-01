import ballerina/config;

function testDottedTableHeaderWithQuotedEntries () returns (string|null) {
    return config:getAsString("hello:sayHello.test.test1.groups");
}

function testColonSeparatedTableHeader () returns (string|null) {
    return config:getAsString("hello:sayHello1:123.groups");
}

function testDottedKeyWithQuotedEntries () returns (string|null) {
    return config:getAsString("hello.test.xxx.groups.123");
}

function testSlashSeparatedKey () returns (string|null) {
    return config:getAsString("a.b.123/pqr/tz");
}

function testSlashSeparatedHeaderAndKey () returns (string|null) {
    return config:getAsString("a/b/r.456/pqr");
}

function testSimpleKey () returns (string|null) {
    return config:getAsString("abc123.testKey");
}
