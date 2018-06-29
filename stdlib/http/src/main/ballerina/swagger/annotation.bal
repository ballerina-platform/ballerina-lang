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


documentation {
    Model for additional Swagger information of a Ballerina service.

    F{{title}} Title of the Swagger definition
    F{{serviceVersion}} Version of the Swagger API
    F{{termsOfService}} Service usage terms and conditions
    F{{contact}} Contact information for the exposed API
    F{{license}} License information for the exposed API
    F{{externalDocs}} Additional external documentation
    F{{tags}} A list of tags used by the specification with additional metadata
    F{{security}} Security requirements for this service
}
public type ServiceInformation record {
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

documentation {
    Model for Swagger contact information.

    F{{name}} Contact name
    F{{email}} Contact email
    F{{url}} Contact web address/page
}
public type Contact record {
    string name,
    string email,
    string url,
};

documentation {
    Model for service licence information.

    F{{name}} License name
    F{{url}} License url
}
public type License record {
    string name,
    string url,
};

documentation {
    Model for service documentation definition.

    F{{description}} Documentation description
    F{{url}} External documentation url
}
public type DocumentationInformation record {
    string description,
    string url,
};

documentation {
    Model for Swagger service tag definition.

    F{{name}} Tag name
    F{{description}} Tag decription
    F{{externalDocs}} Optional documentation on the tag
}
public type Tag record {
    string name,
    string description,
    DocumentationInformation externalDocs,
};

documentation {
    Model for security requirement definition. This is most likely the OAuth scopes.

    F{{name}} Security scheme name
    F{{requirements}} Array of security requirements
}
public type SecurityRequirement record {
    string name,
    string[] requirements,
};

documentation {
    Model for keeping Swagger parameter information.

    F{{inInfo}} Where the parameter is located. Ex: query
    F{{name}} Parameter name
    F{{description}} Description of the parameter
    F{{required}} Is this parameter MUST be present in the request
    F{{discontinued}} Is this parameter deprecated
    F{{allowEmptyValue}} Is an empty value allowed for this parameter. Valid only for query parameters
    F{{schema}} Parameter data type
}
public type ParameterInformation record {
    string inInfo,
    string name,
    string description,
    boolean required,
    boolean discontinued,
    string allowEmptyValue,
    Schema schema,
};

documentation {
    Model for keeping additional Swagger schema information.

    F{{format}} Data format as specified by Swagger data type
    F{{isArray}} Is this an array type schema
    F{{ref}} Schema reference if this schema definition is a reference type definition
}
public type Schema record {
    string format,
    boolean isArray,
    string ref,
};

documentation {
    Model for additional Swagger resource definition.

    F{{tags}} Tags attched to this resource
    F{{summary}} A short summary of what the operation does
    F{{description}} A verbose explanation of the operation behavior
    F{{externalDocs}} Additional documentation for this operation
    F{{parameters}} A list of parameters that are applicable for this operation
}
public type ResourceInformation record {
    string[] tags,
    string summary,
    string description,
    DocumentationInformation externalDocs,
    ParameterInformation[] parameters,
};

documentation {
    Model for keeping Swagger response information.

    F{{code}} Reponse code
    F{{description}} Response description
    F{{response}} Response content
    F{{headers}} Response headers
    F{{examples}} Examples for this response
}
public type Response record {
    string code,
    string description,
    string response,
    Header[] headers,
    Example[] examples,
};

documentation {
    Model for keeping Swagger header definition information.

    F{{required}} Is this a required header
    F{{discontinued}} Is this header deprecated
    F{{description}} Header description
}
public type Header record {
    boolean required,
    boolean discontinued,
    string description,
};

documentation {
    Model for keeping Swagger example information.

    F{{summary}} Short description for the example
    F{{description}} Long description for the example
    F{{value}} Any example value
    F{{externalValue}} A URL that points to the literal example
}
public type Example record {
    string summary,
    string description,
    any value,
    string externalValue,
};

documentation {
    Model for additional Swagger request body details.

    F{{description}} Brief description of the request body
    F{{required}} Determines if the request body is required in the request
    F{{example}} Example of the request body media type
    F{{examples}} Examples of the media type
    F{{schema}} The schema defining the type used for the request body
    F{{encoding}} Encoding and content type details
}
public type requestBody record {
    string description,
    boolean required,
    string example,
    Example[] examples,
    Schema schema,
    Encoding[] encoding,
};

documentation {
    Model for additional Swagger content type definition.

    F{{headers}} Additional information to be provided as headers
    F{{contentType}} The Content-Type for encoding a specific property
    F{{style}} Describes how a specific property value will be serialized depending on its type
    F{{explode}} Should property values of array or object generate separate parameters for each value of the array
    F{{allowReserved}} Determines whether the parameter value SHOULD allow reserved characters
}
public type Encoding record {
    ParameterInformation[] headers,
    string contentType,
    string style,
    boolean explode,
    boolean allowReserved,
};

documentation {
    Configuration elements for client code generation.

    F{{generate}} generates client code if set to true
}
public type ClientInformation record {
    boolean generate = true,
};

documentation {
    Presence of this annotation will mark this endpoint to be used as a service endpoint for client generation
}
public annotation <endpoint> ClientEndpoint;

documentation {
    Annotation to configure client code generation.
}
public annotation <service> ClientConfig ClientInformation;

documentation {
    Annotation for additional Swagger information of a Ballerina service.
}
public annotation <service> ServiceInfo ServiceInformation;

documentation {
    Annotation for additional Swagger information of a Ballerina resource.
}
public annotation <resource> ResourceInfo ResourceInformation;