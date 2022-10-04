import ballerina/module1;

function testFunction() {
    @m
    client "https://postman-echo.com/get?name=projectapiclientplugin" as foo;
}

public type AnnotationType record {
    string foo;
    int bar?;
};

public annotation AnnotationType clientAnnot1 on source client;

public annotation clientAnnot1 on source client;

public annotation commonAnnotation;
