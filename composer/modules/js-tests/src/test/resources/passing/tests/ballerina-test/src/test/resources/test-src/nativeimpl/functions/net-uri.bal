import ballerina.net.uri;

function testEncode (string url) (string, error) {
    var encodedUrl, err = uri:encode(url, "UTF-8");

    if (err != null) {
        return null, err;
    }
    return encodedUrl, null;
}

function testInvalidEncode (string url) (string, error) {
    var encodedUrl, err = uri:encode(url, "abc");

    if (err != null) {
        return null, err;
    }
    return encodedUrl, null;
}

function testDecode (string url) (string, error) {
    var decodedUrl, err = uri:decode(url, "UTF-8");

    if (err != null) {
        return null, err;
    }
    return decodedUrl, null;
}

function testInvalidDecode (string url) (string, error) {
    var decodedUrl, err = uri:decode(url, "abc");

    if (err != null) {
        return null, err;
    }
    return decodedUrl, null;
}