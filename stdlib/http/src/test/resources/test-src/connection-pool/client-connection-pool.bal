import ballerina/http;

http:PoolConfiguration sharedPoolConfig = {};

function testGlobalPoolConfig() returns http:Client[] {
    http:Client httpClient1 = new("http://localhost:8080");
    http:Client httpClient2 = new("http://localhost:8080");
    http:Client httpClient3 = new("http://localhost:8081");
    http:Client[] clients = [httpClient1, httpClient2, httpClient3];
    return clients;
}

function testSharedConfig() returns http:Client[] {
    http:Client httpClient1 = new("http://localhost:8080", config = { poolConfig: sharedPoolConfig });
    http:Client httpClient2 = new("http://localhost:8080", config = { poolConfig: sharedPoolConfig });
    http:Client[] clients = [httpClient1, httpClient2];
    return clients;
}

function testPoolPerClient() returns http:Client[] {
    http:Client httpClient1 = new("http://localhost:8080", config = { poolConfig: { maxActiveConnections: 50 } });
    http:Client httpClient2 = new("http://localhost:8080", config = { poolConfig: { maxActiveConnections: 25 } });
    http:Client[] clients = [httpClient1, httpClient2];
    return clients;
}
