@Description {value: "Model for additional swagger information of a ballerina service"}
@Field {value: "title: Title of the swagger definition"}
@Field {value: "serviceVersion: Version of the swagger API"}
@Field {value: "termsOfService: Service usage terms and conditions"}
@Field {value: "contact: Contact information for the exposed API."}
@Field {value: "license: License information for the exposed API."}
@Field {value: "externalDoc: Additional external documentation."}
@Field {value: "tags: A list of tags used by the specification with additional metadata"}
public type ServiceInformation {
    string title,
    string serviceVersion,
    string description,
    string termsOfService,
    Contact contact,
    License license,
    DocumentationInformation externalDocs,
    Tag[] tags,
    SecurityRequirement[] security,
};

@Description {value: "Model for Swagger contact information"}
@Field {value: "name: Contact name"}
@Field {value: "email: Contact email"}
@Field {value: "url: Contact web address/page"}
public type Contact {
    string name,
    string email,
    string url,
};

@Description {value: "Model for service licence information"}
@Field {value: "name: License name"}
@Field {value: "url: License url"}
public type License {
    string name,
    string url,
};

@Description {value: "Model for service documentation definition"}
@Field {value: "description: Documentation description"}
@Field {value: "url: Documentation url"}
public type DocumentationInformation {
    string description,
    string url,
};

@Description {value: "Model for swagger service tag definition"}
@Field {value: "name: Tag name"}
@Field {value: "description: Tag decription"}
@Field {value: "externalDocs: Optional documentation on the tag"}
public type Tag {
    string name,
    string description,
    DocumentationInformation externalDocs,
};

@Description {value: "Model for security requirement definition. This is most likely the oauth scopes"}
@Field {value: "name: Security scheme name"}
@Field {value: "requirements: Array of security requirements"}
public type SecurityRequirement {
    string name,
    string[] requirements,
};

@Description {value: "Model for keeping swagger parameter information"}
@Field {value: "inInfo: Where the parameter is located. Ex: query"}
@Field {value: "name: parameter name"}
@Field {value: "description: Description of the parameter"}
@Field {value: "required: Is this paramter MUST be present in the request"}
@Field {value: "discontinued: Is this parameter deprecated"}
@Field {value: "allowEmptyValue: is empty values allowed for this parameter. Valid only for query parameters"}
@Field {value: "schema: Parameter data type"}
public type ParameterInformation {
    string inInfo,
    string name,
    string description,
    boolean required,
    boolean discontinued,
    string allowEmptyValue,
    Schema schema,
};

public type Schema {
    string itemType,
    string format,
    boolean isArray,
    string ref,
    string items,
};

@Description {value: "Model for additional swagger resource definition"}
@Field {value: "tags: Tags attched to this resource"}
@Field {value: "summary: A short summary of what the operation does."}
@Field {value: "description: A verbose explanation of the operation behavior"}
@Field {value: "externalDocs: Additional documentation for this operation"}
@Field {value: "parameters: A list of parameters that are applicable for this operation"}
public type ResourceInformation {
    string[] tags,
    string summary,
    string description,
    DocumentationInformation externalDocs,
    ParameterInformation[] parameters,
};

public type Response {
    string code,
    string description,
    string response,
    Header[] headers,
    Example[] examples,
};

public type Header {
    string name,
    string description,
    string headerType,
};

public type Example {
    string exampleType,
    string value,
};

@Description {value: "Model for additional swagger request body details"}
@Field {value: "description: A brief description of the request body"}
@Field {value: "required: Determines if the request body is required in the request"}
@Field {value: "example: Example of the request body media type"}
@Field {value: "examples: Examples of the media type"}
@Field {value: "schema: The schema defining the type used for the request body"}
@Field {value: "encoding: Encoding and content type details"}
public type requestBody {
    string description,
    boolean required,
    string example,
    Example[] examples,
    Schema schema,
    Encoding[] encoding,
};

@Description {value: "Model for additional swagger content type definition"}
@Field {value: "headers: Additional information to be provided as headers"}
@Field {value: "contentType: The Content-Type for encoding a specific property"}
@Field {value: "explode: Should property values of array or object generate separate parameters for each value of the array"}
@Field {value: "allowReserved: Determines whether the parameter value SHOULD allow reserved characters"}
public type Encoding {
    ParameterInformation[] headers,
    string contentType,
    string style,
    boolean explode,
    boolean allowReserved,
};

@Description {value: "Configuration elements for client code generation"}
@Field {value: "generate: generates client code if set to true"}
public type ClientInformation {
    boolean generate = true,
};

@Description {value: "Presence of this annotation will mark this endpoint to be used as a service endpoint for client generation"}
public annotation <endpoint> ClientEndpoint;

@Description {value: "Annotation to configure client code generation"}
public annotation <service> ClientConfig ClientInformation;

@Description {value: "Annotation for additional swagger information of a ballerina service"}
public annotation <service> ServiceInfo ServiceInformation;

@Description {value: "Annotation for additional swagger information of a ballerina resource"}
public annotation <resource> ResourceInfo ResourceInformation;