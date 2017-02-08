function getProduct() (json) {
    message m;
    json payload;
    payload = `{"Product": {"ID": "123456", "Name": "XYZ","Description": "Sample product."}}`;
    return payload;
}