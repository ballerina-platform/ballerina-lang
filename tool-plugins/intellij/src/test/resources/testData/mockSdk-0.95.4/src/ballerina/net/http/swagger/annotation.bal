package ballerina.net.http.swagger;

import ballerina/net.http;

public annotation ServiceInfo attach service<http> {
    string title;
    string serviceVersion;
    string description;
    string termsOfService;
    Contact contact;
    License license;
    ExternalDoc externalDoc;
    Tag[] tags;
    Organization organization;
    Developer[] developers;
}

public annotation Contact {
    string name;
    string email;
    string url;
}

public annotation License {
    string name;
    string url;
}

public annotation ExternalDoc {
    string description;
    string url;
}

public annotation Tag {
    string name;
    string description;
}

public annotation Organization {
    string name;
    string url;
}

public annotation Developer {
    string name;
    string email;
}

public annotation Swagger attach service<http> {
    string swaggerVersion;
    SwaggerExtension[] extension;
}

public annotation SwaggerExtension {
    string target;
}

public annotation ServiceConfig attach service<http> {
    string host;
    string[] schemes;
    string interface;
    Authorization[] authorizations;
}

public annotation Authorization {
    string name;
    string description;
    string authType;
    string apiName;
    string in;
    string flow;
    string authorizationUrl;
    string tokenUrl;
    AuthorizationScope[] authorizationScopes;
}

public annotation AuthorizationScope {
    string name;
    string description;
}

public annotation ResourceConfig attach resource {
    string[] schemes;
    Authorization[] authorizations;
    string name;
    string[] scopes;
}

public annotation ParametersInfo attach resource {
    ParameterInfo[] value;
}

public annotation ParameterInfo {
    string in;
    string name;
    string description;
    boolean required;
    string allowEmptyValue;
    string parameterType;
    string format;
    string collectionFormat;
    Item[] items;
}

public annotation Item {
    string itemType;
    string format;
    string collectionFormat;
    string items;
}

public annotation ResourceInfo attach resource {
    string[] tags;
    string summary;
    string description;
    ExternalDoc externalDoc;
}

public annotation Responses attach resource {
    Response[] value;
}

public annotation Response {
    string code;
    string description;
    string response;
    Header[] headers;
    Example[] examples;
}

public annotation Header {
    string name;
    string description;
    string headerType;
}

public annotation Example {
    string exampleType;
    string value;
}