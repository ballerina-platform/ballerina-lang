import ballerina/mime;

function testAddHeader (string headerName, string headerValue, string headerNameToBeUsedForRetrieval) returns string {
    mime:Entity entity = new;
    entity.addHeader(headerName, headerValue);
    return entity.getHeader(headerNameToBeUsedForRetrieval);
}

function testAddingMultipleHeaders () returns (string, string , string ) {
    mime:Entity entity = new;
    entity.addHeader("header1", "value1");
    entity.addHeader("header2", "value2");
    entity.addHeader("header3", "value3");
    return (entity.getHeader("header1"), entity.getHeader("header2"), entity.getHeader("header3"));
}

function testAddingMultipleValuesToSameHeader () returns (string[], string ) {
    mime:Entity entity = new;
    entity.addHeader("heAder1", "value1");
    entity.addHeader("header1", "value2");
    entity.addHeader("header1", "value3");
    entity.addHeader("hEader2", "value3");
    entity.addHeader("headeR2", "value4");
    return (entity.getHeaders("header1"), entity.getHeader("header2"));
}

function testSetHeader () returns (string[], string ) {
    mime:Entity entity = new;
    entity.setHeader("HeADEr2", "totally different value");
    return (entity.getHeaders("header1"), entity.getHeader("header2"));
}

function testSetHeaderAfterAddHeader () returns (string[], string ) {
    mime:Entity entity = new;
    entity.addHeader("heAder1", "value1");
    entity.addHeader("header1", "value2");
    entity.addHeader("header1", "value3");
    entity.addHeader("hEader2", "value3");
    entity.addHeader("headeR2", "value4");
    entity.setHeader("HeADEr2", "totally different value");
    return (entity.getHeaders("header1"), entity.getHeader("header2"));
}


function testAddHeaderAfterSetHeader () returns (string[], string ) {
    mime:Entity entity = new;
    entity.addHeader("heAder1", "value1");
    entity.addHeader("header1", "value2");
    entity.addHeader("header1", "value3");
    entity.addHeader("hEader2", "value3");
    entity.setHeader("HeADEr2", "totally different value");
    entity.addHeader("headeR2", "value4");
    return (entity.getHeaders("header2"), entity.getHeader("header2"));
}

function testRemoveHeader () returns (string[], string) {
    mime:Entity entity = new;
    entity.addHeader("heAder1", "value1");
    entity.addHeader("header1", "value2");
    entity.addHeader("header1", "value3");
    entity.addHeader("hEader2", "value3");
    entity.addHeader("headeR2", "value4");
    entity.setHeader("HeADEr2", "totally different value");
    entity.removeHeader("HEADER1");
    entity.removeHeader("NONE_EXISTENCE_HEADER");
    return (entity.getHeaders("header1"), entity.getHeader("header2"));
}

function testNonExistenceHeader () returns string {
    mime:Entity entity = new;
    entity.addHeader("heAder1", "value1");
    return entity.getHeader("header");
}

function testGetCopyOfAllHeaders () returns (map) {
    mime:Entity entity = new;
    entity.addHeader("heAder1", "value1");
    entity.addHeader("header1", "value2");
    entity.addHeader("header1", "value3");
    entity.addHeader("hEader2", "value3");
    entity.addHeader("headeR2", "value4");
    entity.addHeader("HeADEr2", "totally different value");
    entity.addHeader("HEADER3", "testVal");
    return entity.getCopyOfAllHeaders();
}

function testManipulatingReturnHeaders () returns (map) {
    mime:Entity entity = new;
    entity.addHeader("heAder1", "value1");
    entity.addHeader("header1", "value2");
    entity.addHeader("header1", "value3");
    entity.addHeader("hEader2", "value3");
    entity.addHeader("headeR2", "value4");
    entity.addHeader("HeADEr2", "totally different value");
    entity.addHeader("HEADER3", "testVal");
    map headerMap = entity.getCopyOfAllHeaders();
    _ = headerMap.remove("HeADEr2");
    _ = headerMap.remove("HEADER3");
    return entity.getCopyOfAllHeaders();
}

function testHasHeader() returns boolean{
    mime:Entity entity = new;
    entity.addHeader("heAder1", "value1");
    return entity.hasHeader("header1");
}

function testHasHeaderForNonExistenceHeader() returns boolean{
    mime:Entity entity = new;
    entity.addHeader("heAder1", "value1");
    return entity.hasHeader("header2");
}
