import ballerina.net.http;

function testAddHeader (http:PushPromise promise, string key, string value) (http:PushPromise) {
    promise.addHeader(key, value);
    return promise;
}

function testGetHeader (http:PushPromise promise, string key) (string) {
    string contentType = promise.getHeader(key);
    return contentType;
}

function testGetHeaders (http:PushPromise promise, string key) (string[]) {
    return promise.getHeaders(key);
}

function testRemoveHeader (http:PushPromise promise, string key) (http:PushPromise) {
    promise.removeHeader(key);
    return promise;
}

function testRemoveAllHeaders (http:PushPromise promise) (http:PushPromise) {
    promise.removeAllHeaders();
    return promise;
}

function testSetHeader (http:PushPromise promise, string key, string value) (http:PushPromise) {
    promise.setHeader(key, value);
    return promise;
}
