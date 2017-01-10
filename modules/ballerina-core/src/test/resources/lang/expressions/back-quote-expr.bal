import ballerina.lang.message;

function getProduct() (message) {
    message m;
    json payload;
    payload = `{"Product": {"ID": "123456", "Name": "XYZ","Description": "Sample product."}}`;
    m = new message;
    message:setJsonPayload(m, payload);
    return m;
}