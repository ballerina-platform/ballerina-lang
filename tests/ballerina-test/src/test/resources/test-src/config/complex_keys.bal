import ballerina/config;

function testDottedTableHeaderWithQuotedEntries () returns (string|()) {
    return config:getAsString("hello:sayHello.test.test1.groups");
}

function testColonSeparatedTableHeader () returns (string|()) {
    return config:getAsString("hello:sayHello1:123.groups");
}

function testDottedKeyWithQuotedEntries () returns (string|()) {
    return config:getAsString("hello.test.xxx.groups.123");
}

function testSlashSeparatedKey () returns (string|()) {
    return config:getAsString("a.b.123/pqr/tz");
}

function testSlashSeparatedHeaderAndKey () returns (string|()) {
    return config:getAsString("a/b/r.456/pqr");
}

function testSimpleKey () returns (string|()) {
    return config:getAsString("abc123.testKey");
}
