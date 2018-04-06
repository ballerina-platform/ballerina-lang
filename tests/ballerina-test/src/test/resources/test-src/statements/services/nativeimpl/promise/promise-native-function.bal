import ballerina/http;

function testAddHeader (http:PushPromise promise, string key, string value) returns http:PushPromise {
    promise.addHeader(key, value);
    return promise;
}

function testGetHeader (http:PushPromise promise, string key) returns string {
    string contentType = promise.getHeader(key);
    return contentType;
}

function testGetHeaders (http:PushPromise promise, string key) returns string[] {
    return promise.getHeaders(key);
}

function testRemoveHeader (http:PushPromise promise, string key) returns http:PushPromise {
    promise.removeHeader(key);
    return promise;
}

function testRemoveAllHeaders (http:PushPromise promise) returns http:PushPromise {
    promise.removeAllHeaders();
    return promise;
}

function testSetHeader (http:PushPromise promise, string key, string value) returns http:PushPromise {
    promise.setHeader(key, value);
    return promise;
}
