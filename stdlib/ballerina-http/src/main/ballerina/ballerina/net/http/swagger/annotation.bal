package ballerina.net.http.swagger;

import ballerina.net.http;

@Description {value: "Annotation to hold additional swagger information of a ballerina service"}
@Field {value: "title: Title of the swagger definition"}
@Field {value: "serviceVersion: Version of the swagger API"}
@Field {value: "termsOfService: Service usage terms and conditions"}
@Field {value: "contact: Contact information for the exposed API."}
@Field {value: "license: License information for the exposed API."}
@Field {value: "externalDoc: Additional external documentation."}
@Field {value: "tags: A list of tags used by the specification with additional metadata"}
public annotation serviceInfo attach service<http> {
    string title;
    string serviceVersion;
    string description;
    string termsOfService;
    contact contact;
    license license;
    externalDoc externalDoc;
    tag[] tags;
}

public annotation contact {
    string name;
    string email;
    string url;
}

public annotation license {
    string name;
    string url;
}

public annotation externalDoc {
    string description;
    string url;
}

public annotation tag {
    string name;
    string description;
    externalDoc externalDoc;
}

public annotation organization {
    string name;
    string url;
}

public annotation developer {
    string name;
    string email;
}

public annotation swagger attach service<http> {
    string swaggerVersion;
    swaggerExtension[] extension;
}

public annotation swaggerExtension {
    string target;
}

public annotation serviceConfig attach service<http> {
    string host;
    string[] schemes;
    string interface;
    authorization[] authorizations;
}

public annotation authorization {
    string name;
    string description;
    string authType;
    string apiName;
    string inFlow;
    string flow;
    string authorizationUrl;
    string tokenUrl;
    authorizationScope[] authorizationScopes;
}

public annotation authorizationScope {
    string name;
    string description;
}

public annotation securityRequirement {
    string name;
    string[] requirements;
}

public annotation resourceConfig attach resource {
    string[] schemes;
    authorization[] authorizations;
    string name;
    string[] scopes;
}

public annotation parametersInfo attach resource {
    parameterInfo[] value;
}

public annotation parameterInfo {
    string inInfo;
    string name;
    string description;
    boolean required;
    boolean deprecated;
    string allowEmptyValue;
    string parameterType;
    string format;
    string collectionFormat;
    schema[] schemas;
}

public annotation schema {
    string itemType;
    string format;
    boolean isArray;
    string ref;
    string items;
}

public annotation resourceInfo attach resource {
    string tags;
    string summary;
    string description;
    boolean deprecated;
    externalDoc externalDoc;
    parameterInfo parameters;
}

public annotation responses attach resource {
    response[] value;
}

public annotation response {
    string code;
    string description;
    string response;
    header[] headers;
    example[] examples;
}

public annotation header {
    string name;
    string description;
    string headerType;
}

public annotation example {
    string exampleType;
    string value;
}