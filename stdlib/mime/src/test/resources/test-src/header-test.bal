import ballerina/mime;

function testAddHeader (string headerName, string headerValue, string headerNameToBeUsedForRetrieval) returns @tainted string {
    mime:Entity entity = new;
    entity.addHeader(headerName, headerValue);
    return entity.getHeader(headerNameToBeUsedForRetrieval);
}

function testAddingMultipleHeaders () returns @tainted [string, string , string ] {
    mime:Entity entity = new;
    entity.addHeader("header1", "value1");
    entity.addHeader("header2", "value2");
    entity.addHeader("header3", "value3");
    return [entity.getHeader("header1"), entity.getHeader("header2"), entity.getHeader("header3")];
}

function testAddingMultipleValuesToSameHeader () returns @tainted [string[], string ] {
    mime:Entity entity = new;
    entity.addHeader("heAder1", "value1");
    entity.addHeader("header1", "value2");
    entity.addHeader("header1", "value3");
    entity.addHeader("hEader2", "value3");
    entity.addHeader("headeR2", "value4");
    return [entity.getHeaders("header1"), entity.getHeader("header2")];
}

function testSetHeader () returns @tainted [string[], string] {
    mime:Entity entity = new;
    entity.setHeader("HeADEr2", "totally different value");
    return [entity.getHeaders("header1"), entity.getHeader("header2")];
}

function testSetHeaderAfterAddHeader () returns @tainted [string[], string] {
    mime:Entity entity = new;
    entity.addHeader("heAder1", "value1");
    entity.addHeader("header1", "value2");
    entity.addHeader("header1", "value3");
    entity.addHeader("hEader2", "value3");
    entity.addHeader("headeR2", "value4");
    entity.setHeader("HeADEr2", "totally different value");
    return [entity.getHeaders("header1"), entity.getHeader("header2")];
}


function testAddHeaderAfterSetHeader () returns @tainted [string[], string] {
    mime:Entity entity = new;
    entity.addHeader("heAder1", "value1");
    entity.addHeader("header1", "value2");
    entity.addHeader("header1", "value3");
    entity.addHeader("hEader2", "value3");
    entity.setHeader("HeADEr2", "totally different value");
    entity.addHeader("headeR2", "value4");
    return [entity.getHeaders("header2"), entity.getHeader("header2")];
}

function testRemoveHeader () returns @tainted [string[], string] {
    mime:Entity entity = new;
    entity.addHeader("heAder1", "value1");
    entity.addHeader("header1", "value2");
    entity.addHeader("header1", "value3");
    entity.addHeader("hEader2", "value3");
    entity.addHeader("headeR2", "value4");
    entity.setHeader("HeADEr2", "totally different value");
    entity.removeHeader("HEADER1");
    entity.removeHeader("NONE_EXISTENCE_HEADER");
    return [entity.getHeaders("header1"), entity.getHeader("header2")];
}

function testNonExistenceHeader () returns @tainted string {
    mime:Entity entity = new;
    entity.addHeader("heAder1", "value1");
    return entity.getHeader("header");
}

function testGetHeaderNames () returns @tainted string[] {
    mime:Entity entity = new;
    entity.addHeader("heAder1", "value1");
    entity.addHeader("header1", "value2");
    entity.addHeader("header1", "value3");
    entity.addHeader("hEader2", "value3");
    entity.addHeader("headeR2", "value4");
    entity.addHeader("HeADEr2", "totally different value");
    entity.addHeader("HEADER3", "testVal");
    return entity.getHeaderNames();
}

function testManipulateHeaders () returns @tainted string[] {
    mime:Entity entity = new;
    entity.addHeader("heAder1", "value1");
    entity.addHeader("header1", "value2");
    entity.addHeader("header1", "value3");
    entity.addHeader("hEader2", "value3");
    entity.addHeader("headeR2", "value4");
    entity.addHeader("HeADEr2", "totally different value");
    entity.addHeader("HEADER3", "testVal");
    string[] headerNames = entity.getHeaderNames();
    foreach var header in headerNames {
        entity.removeHeader(<@untainted string> header);
    }

    return entity.getHeaderNames();
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

function testHeaderWithNewEntity() returns @tainted [boolean, string[]] {
    mime:Entity entity = new;
    return [entity.hasHeader("header2"), entity.getHeaderNames()];
}

function testTrailingAddHeader(string headerName, string headerValue, string retrieval) returns @tainted string {
    mime:Entity entity = new;
    entity.addHeader(headerName, headerValue, position = "trailing");
    return entity.getHeader(retrieval, position = "trailing");
}

function testAddingMultipleValuesToSameTrailingHeader() returns @tainted [string[], string ] {
    mime:Entity entity = new;
    entity.addHeader("heAder1", "value1", position = "trailing");
    entity.addHeader("header1", "value2", position = "trailing");
    return [entity.getHeaders("header1", position = "trailing"), entity.getHeader("header1", position = "trailing")];
}

function testSetTrailingHeaderAfterAddHeader() returns @tainted [string[], string] {
    mime:Entity entity = new;
    entity.addHeader("heAder1", "value1", position = "trailing");
    entity.addHeader("header1", "value2", position = "trailing");
    entity.addHeader("hEader2", "value3", position = "trailing");
    entity.setHeader("HeADEr2", "totally different value", position = "trailing");
    return [entity.getHeaders("header1", position = "trailing"), entity.getHeader("header2", position = "trailing")];
}

    function testRemoveTrailingHeader() returns @tainted [string[], string] {
    mime:Entity entity = new;
    entity.addHeader("heAder1", "value1", position = "trailing");
    entity.addHeader("header1", "value2", position = "trailing");
    entity.addHeader("header1", "value3", position = "trailing");
    entity.addHeader("hEader2", "value3", position = "trailing");
    entity.addHeader("headeR2", "value4", position = "trailing");
    entity.setHeader("HeADEr2", "totally different value", position = "trailing");
    entity.removeHeader("HEADER1", position = "trailing");
    entity.removeHeader("NONE_EXISTENCE_HEADER", position = "trailing");
    return [entity.getHeaders("header1", position = "trailing"), entity.getHeader("header2", position = "trailing")];
}

function testNonExistenceTrailingHeader() returns @tainted string {
    mime:Entity entity = new;
    entity.addHeader("heAder1", "value1", position = mime:TRAILING);
    return entity.getHeader("header", position = mime:TRAILING);
}

function testGetTrailingHeaderNames() returns @tainted string[] {
    mime:Entity entity = new;
    entity.addHeader("heAder1", "value1", position = mime:TRAILING);
    entity.addHeader("header1", "value2", position = mime:TRAILING);
    entity.addHeader("header1", "value3", position = mime:TRAILING);
    entity.addHeader("hEader2", "value3", position = mime:TRAILING);
    entity.addHeader("headeR2", "value4", position = mime:TRAILING);
    entity.addHeader("HeADEr2", "totally different value", position = mime:TRAILING);
    entity.addHeader("HEADER3", "testVal", position = mime:TRAILING);
    return entity.getHeaderNames(position = mime:TRAILING);
}

function testTrailingHasHeader() returns boolean{
    mime:Entity entity = new;
    entity.addHeader("heAder1", "value1", position = mime:TRAILING);
    return entity.hasHeader("header1", position = mime:TRAILING);
}

function testTrailingHeaderWithNewEntity() returns @tainted [boolean, string[]] {
    mime:Entity entity = new;
    return [entity.hasHeader("header2", position = mime:TRAILING), entity.getHeaderNames(position = mime:TRAILING)];
}
