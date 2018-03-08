import ballerina.mime;

function testAddHeader (string headerName, string headerValue, string headerNameToBeUsedForRetrieval) (string) {
    mime:Entity entity = {};
    entity.addHeader(headerName, headerValue);
    return entity.getHeader(headerNameToBeUsedForRetrieval);
}

function testAddingMultipleHeaders () (map) {
    mime:Entity entity = {};
    entity.addHeader("header1", "value1");
    entity.addHeader("header2", "value2");
    entity.addHeader("header3", "value3");
    return entity.getAllHeaders();
}

function testAddingMultipleValuesToSameHeader () (map, string[], string) {
    mime:Entity entity = {};
    entity.addHeader("heAder1", "value1");
    entity.addHeader("header1", "value2");
    entity.addHeader("header1", "value3");
    entity.addHeader("hEader2", "value3");
    entity.addHeader("headeR2", "value4");
    return entity.getAllHeaders(), entity.getHeaders("header1"), entity.getHeader("header2");
}

function testSetHeader () (map, string[], string) {
    mime:Entity entity = {};
    entity.addHeader("heAder1", "value1");
    entity.addHeader("header1", "value2");
    entity.addHeader("header1", "value3");
    entity.addHeader("hEader2", "value3");
    entity.addHeader("headeR2", "value4");
    entity.setHeader("HeADEr2", "totally different value");
    return entity.getAllHeaders(), entity.getHeaders("header1"), entity.getHeader("header2");
}

function testRemoveHeader() (map, string[], string) {
    mime:Entity entity = {};
    entity.addHeader("heAder1", "value1");
    entity.addHeader("header1", "value2");
    entity.addHeader("header1", "value3");
    entity.addHeader("hEader2", "value3");
    entity.addHeader("headeR2", "value4");
    entity.setHeader("HeADEr2", "totally different value");
    entity.removeHeader("HEADER1");
    entity.removeHeader("NONE_EXISTENCE_HEADER");
    return entity.getAllHeaders(), entity.getHeaders("header1"), entity.getHeader("header2");
}

function testNonExistenceHeader() (string) {
    mime:Entity entity = {};
    entity.addHeader("heAder1", "value1");
    return entity.getHeader("header2");
}
