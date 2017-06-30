package ballerina.net.http.swagger;

annotation ServiceInfo attach service {
    string title;
    string version;
    string description;
    string termsOfService;
    Contact contact;
    License license;
    ExternalDoc externalDoc;
    Tag[] tag;
    Organization organization;
    Developer[] developers;
}

annotation Contact {
    string name;
    string email;
    string url;
}

annotation License {
    string name;
    string url;
}

annotation ExternalDoc {
    string description;
    string url;
}

annotation Tag {
    string name;
    string description;
}

annotation Organization {
    string name;
    string url;
}

annotation Developer {
    string name;
    string email;
}


annotation Swagger attach service {
    string version;
    SwaggerExtension[] extension;
}

annotation SwaggerExtension {
    string target;
}


annotation ServiceConfig attach service {
    string host;
    string[] schemes;
    string interface;
    Authorization[] authorizations;
}

annotation Authorization {
    string name;
    string description;
    string type;
    string apiName;
    string in;
    string flow;
    string authorizationUrl;
    string tokenUrl;
    AuthorizationScope[] authorizationScopes;
}

annotation AuthorizationScope {
    string name;
    string description;
}

annotation ResourceConfig attach resource {
    string[] schemes;
    Authorization[] authorizations;
    string name;
    string[] scopes;
}

annotation ParametersInfo attach resource {
    ParameterInfo[] value;
}

annotation ParameterInfo {
    string in;
    string name;
    string description;
    boolean required;
    string allowEmptyValue;
    string type;
    string format;
    string collectionFormat;
    Item[] items;
}

annotation Item {
    string type;
    string format;
    string collectionFormat;
    string items;
}

annotation ResourceInfo attach resource {
    string[] tags;
    string summary;
    string description;
    ExternalDoc[] externalDocs;
}

annotation Responses attach resource {
    Response[] value;
}

annotation Response {
    string code;
    string description;
    string response;
    Header[] headers;
    Example[] examples;
}

annotation Header {
    string name;
    string description;
    string type;
}

annotation Example {
    string type;
    string value;
}