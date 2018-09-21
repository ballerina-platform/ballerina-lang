// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.


# Model for additional Swagger information of a Ballerina service.
#
# + title - Title of the Swagger definition
# + serviceVersion - Version of the Swagger API
# + termsOfService - Service usage terms and conditions
# + contact - Contact information for the exposed API
# + license - License information for the exposed API
# + externalDocs - Additional external documentation
# + tags - A list of tags used by the specification with additional metadata
# + security - Security requirements for this service
public type ServiceInformation record {
    string title;
    string serviceVersion;
    string description;
    string termsOfService;
    Contact contact;
    License license;
    DocumentationInformation externalDocs;
    Tag[] tags;
    SecurityRequirement[] security;
    !...
};

# Model for Swagger contact information.
#
# + name - Contact name
# + email - Contact email
# + url - Contact web address/page
public type Contact record {
    string name;
    string email;
    string url;
    !...
};

# Model for service licence information.
#
# + name - License name
# + url - License url
public type License record {
    string name;
    string url;
    !...
};

# Model for service documentation definition.
#
# + description - Documentation description
# + url - External documentation url
public type DocumentationInformation record {
    string description;
    string url;
    !...
};

# Model for Swagger service tag definition.
#
# + name - Tag name
# + description - Tag decription
# + externalDocs - Optional documentation on the tag
public type Tag record {
    string name;
    string description;
    DocumentationInformation externalDocs;
    !...
};

# Model for security requirement definition. This is most likely the OAuth scopes.
#
# + name - Security scheme name
# + requirements - Array of security requirements
public type SecurityRequirement record {
    string name;
    string[] requirements;
    !...
};

# Model for keeping Swagger parameter information.
#
# + inInfo - Where the parameter is located. Ex: query
# + name - Parameter name
# + description - Description of the parameter
# + required - Is this parameter MUST be present in the request
# + discontinued - Is this parameter deprecated
# + allowEmptyValue - Is an empty value allowed for this parameter. Valid only for query parameters
# + schema - Parameter data type
public type ParameterInformation record {
    string inInfo;
    string name;
    string description;
    boolean required;
    boolean discontinued;
    string allowEmptyValue;
    Schema schema;
    !...
};

# Model for keeping additional Swagger schema information.
#
# + format - Data format as specified by Swagger data type
# + isArray - Is this an array type schema
# + ref - Schema reference if this schema definition is a reference type definition
public type Schema record {
    string format;
    boolean isArray;
    string ref;
    !...
};

# Model for additional Swagger resource definition.
#
# + tags - Tags attched to this resource
# + summary - A short summary of what the operation does
# + description - A verbose explanation of the operation behavior
# + externalDocs - Additional documentation for this operation
# + parameters - A list of parameters that are applicable for this operation
public type ResourceInformation record {
    string[] tags;
    string summary;
    string description;
    DocumentationInformation externalDocs;
    ParameterInformation[] parameters;
    !...
};

# Model for keeping Swagger response information.
#
# + code - Reponse code
# + description - Response description
# + response - Response content
# + headers - Response headers
# + examples - Examples for this response
public type Response record {
    string code;
    string description;
    string response;
    Header[] headers;
    Example[] examples;
    !...
};

# Model for keeping Swagger header definition information.
#
# + required - Is this a required header
# + discontinued - Is this header deprecated
# + description - Header description
public type Header record {
    boolean required;
    boolean discontinued;
    string description;
    !...
};

# Model for keeping Swagger example information.
#
# + summary - Short description for the example
# + description - Long description for the example
# + value - Any example value
# + externalValue - A URL that points to the literal example
public type Example record {
    string summary;
    string description;
    any value;
    string externalValue;
    !...
};

# Model for additional Swagger request body details.
#
# + description - Brief description of the request body
# + required - Determines if the request body is required in the request
# + example - Example of the request body media type
# + examples - Examples of the media type
# + schema - The schema defining the type used for the request body
# + encoding - Encoding and content type details
public type requestBody record {
    string description;
    boolean required;
    string example;
    Example[] examples;
    Schema schema;
    Encoding[] encoding;
    !...
};

# Model for additional Swagger content type definition.
#
# + headers - Additional information to be provided as headers
# + contentType - The Content-Type for encoding a specific property
# + style - Describes how a specific property value will be serialized depending on its type
# + explode - Should property values of array or object generate separate parameters for each value of the array
# + allowReserved - Determines whether the parameter value SHOULD allow reserved characters
public type Encoding record {
    ParameterInformation[] headers;
    string contentType;
    string style;
    boolean explode;
    boolean allowReserved;
    !...
};

# Configuration elements for client code generation.
#
# + generate - generates client code if set to true
public type ClientInformation record {
    boolean generate = true;
    !...
};

# Presence of this annotation will mark this endpoint to be used as a service endpoint for client generation
public annotation <endpoint> ClientEndpoint;

# Annotation to configure client code generation.
public annotation <service> ClientConfig ClientInformation;

# Annotation for additional Swagger information of a Ballerina service.
public annotation <service> ServiceInfo ServiceInformation;

# Annotation for additional Swagger information of a Ballerina resource.
public annotation <resource> ResourceInfo ResourceInformation;