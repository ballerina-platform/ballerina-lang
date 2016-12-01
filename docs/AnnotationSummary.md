# Introduction to Ballerina Annotations. 

## Introductions

Ballerina `Annotations` are from of metadata, that attached to a Ballerina construct. By design, Ballerina considers all
annotations as optional, hence A Ballerina Program(Service ?) should be able to run without any annotation. 

Ballerina `Annotations` can be used to annotate following Ballerina constructs.   
 
 * Service
 * Resource
 * Connectors
 * Connections
 * Actions
 * Functions
 * User Defined Types
 * Variables
 

All Ballerina Annotations start with Character `@` (at) and it has following Syntax. 

```
@AnnotationName [ ( PrimitiveValue | "A String" | Array | key = value [, key = value] )]
```

Here `value` can be one of the followings:
* Primitive value. (i.e. integer, long, float, double, boolean)
* A String. (e.g. "aValue")
* Another Annotation. (This can't be the parent or enclosing annotation)
* An Array of one of the above. (e.g. { "String 1", "String 2" })

E.g:
```
    @AnnotationOne
    
    @AnnotationTwo("Value of two")
    
    @AnnotationThree( 
        keyInt = 1 ,
        keyString = "second value",
        keyAnnotation = @InnerAnnotation("Inner Annotation Value")
    )
    
    @AnnotationFour({ "value1" , "value2"})
    
    @AnnotationFive( 
        keyStringArray = { "value 1" , "value 2"} ,
        keyAnnotationArray = {
            @InnerAnnotation( innerKeyInt = 1, innerKeyString = "A String"),
            @InnerAnnotation( innerKeyInt = 1, innerKeyString = "A String")
        }
    )
```

In Ballerina annotations are divided into two categories.

* Documentation Annotations (Doc Annotation)
    - These annotations represent structured meta information about the Ballerina constructs that they annotated.(Similar to
     Java Doc comment.) 
* Config Annotations
    - Config annotations denote additional configuration/behavior instructions for Ballerina runtime.
     (e.g Apply Security, Circuit Breaker, etc) By defining the config annotations for a construct, Ballerina developer 
     can instruct to the Ballerina runtime, to alter default behavior or/and apply Quality of services configuration.
     
Note: By design Doc annotation and Config annotation are defined separately. This will improve readability and performance
 of the system.  

### Swagger 2.0 Support

Ballerina Annotations represent a supper set of Open API Specification 2.0 (AKA Swagger 2.0) format. This allows developers
to Generate a Ballerina service skeleton from a Swagger 2.0 definition and/or Swagger 2.0 definition from a Ballerina service.
 
## Supported Annotations.

### Service Annotations. 

Followings are the service level annotations.

* Service Info
* Service Config
* Path
* Consumes
* Produces

#### Service Info

A Doc annotation, which describes meta information about a Ballerina Service. This a supper set of Swagger 2.0 representation. 

Syntax: 
```
@ServiceInfo(
    title = "Test Example Service" ,
    version = "1.0.0" ,
    description = "This is a short description about test example service" ,
    termOfService = "http://example.com/services/Test/terms.html" ,
    contact = @Contact( name = "WSO2 support" , email = "support@wso2.com" , url = "http://wso2.com/contact/" ),
    license = @License( name = "Apache 2", url = "http://www.apache.org/licenses/LICENSE-2.0") ,
    externalDoc = @ExternalDoc( description = "Wso2 Ballerina Documentation", url = "https://docs.wso2.com/ballerina" ) , 
    tags = {
        @Tag(
            name = "test" ,
            description = "this is a test service" ,
            doc = @Doc( ... )
        ) ,
        @Tag( ... )
    }
    organization = @Organization( name = "WSO2 inc.", url = "https://wso2.com") ,
    developers = {
        @Developer( name = "" , email = "")
    } ,
)
@Swagger(
    version = "2.0" , 
    extenstions = {
        @SwaggerExtension(
            target = "json-path to swagger element"
            [, anyKey = anyValue]+
        ) ,
        @SwaggerExtenstion( ... )
    }
)
service TestService {
 ...
}
```

Following section describes each field defined in `@ServiceInfo` annotation. 

| Ballerina Field | Type | Description | Swagger Field (Json Path) |
|---|:---:|---|---|
| title | string | **Required.** The title of the Ballerina Service. | $.info.title |
| version | string | **Required.** The version of the Ballerina Service. | $.info.version |
| description | string | A short description about the Ballerina Service. | $.info.description |
| termsOfService | string | Text or URL for the Terms of Services for the Ballerina Service. | $.info.termsOfService |
| contact | @Contact | The Contact information for Ballerina Service. | $.info.contact |
| license | @License | The License information for Ballerina Service. | $.info.license |
| externalDoc | @ExternalDoc |  An link to external documentation which describes annotated service. | $.externalDocs |
| tag | @Tag[] |  A list of tags used by the specification with additional metadata. | $.tags |
| organization | @Organization | Organization this service belongs to. | $.info.x-organization |
| developers | @Developers[] | Information about developers involved. | $.info.x-developers |

_@Contact_

| Ballerina Field | Type | Description | Swagger Field (Json Path) |
|---|:---:|---|---|
| name | string | Name of the contact person or organization. | $.info.contact.name |
| email | string | email of the contact person or organization. | $.info.contact.x-email |
| url | string | An URL pointing to contact information. | $.info.contact.url |

_@License_

| Ballerina Field | Type | Description | Swagger Field |
|---|:---:|---|---|
| name | string | Name of the License used for Ballerina Service. | $.info.license.name |
| url | string | An URL pointing to License information. | $.info.license.url |

_@ExternalDoc_

| Ballerina Field | Type | Description | Swagger Field |
|---|:---:|---|---|
| description | string | a description about the target documentation. | parent.description |
| url | string | **Required.** URL is pointing to target documentation. | parent.url |

_@Tag_

| Ballerina Field | Type | Description | Swagger Field |
|---|:---:|---|---|
| name | string | **Required.** Name of tag. | $.tags\[position\].name |
| description | string | Description explaining current tag. | $.tags\[position\].description |
| doc | @Doc | Additional external documentation link explaining current tag. | $.tags\[position\].externalDocs |

_@Organization_

| Ballerina Field | Type | Description | Swagger Field |
|---|:---:|---|---|
| name | string | Name of the Organization. | $.info.x-organization.name |
| url | string | An URL pointing to the Organization website. | $.info.x-organization.url |

_@Developer_

| Ballerina Field | Type | Description | Swagger Field |
|---|:---:|---|---|
| name | string | Name of the developer. | $.info.x-developers\[position\].name |
| email | string | An email address of the Developer. | $.info.x-developers\[position\].email |

Following section describes each field defined in `@Swagger` annotation. This annotation represents all the vendor 
specific custom swagger extensions. 

| Ballerina Field | Type | Description | Swagger Field (Json Path) |
|---|:---:|---|---|
| version | string | **Required.** Specifies the Swagger Specification version to be used. If `@Swagger` annotation is not defined, default value is "2.0" | $.swagger |
| extension | @SwaggerExtension[] | List of Swagger extension for specific swagger field. | Dynamic |

@SwaggerExtension

| Ballerina Field | Type | Description | Swagger Field (Json Path) |
|---|:---:|---|---|
| target | string | **Required.** Json path of the swagger field. | target |
| anyKey | any | List of Swagger extension for specific swagger field. | target.x-anykey |


#### Service Config

A Config annotation, which represents common configuration for a Ballerina Service. 

Syntax:
```
@ServiceConfig (
    host = "http://example.com/sample/service" , 
    schemes = {"http", "https"} ,
    interface = "Interface Key or path to Interface." 
    authorizationsConfigurations = { 
        @AuthorizationsConfiguration(
            name = "anUniqueName", 
            type = "basic|apiKey|oauth2|...", 
            description = "A Description."

            [, flow = "implicit|password|application|accessCode" , 
               authorizationUrl = "..." , 
               tokenUrl = "..." , 
               authorizationScopes = {
                    @AuthorizationScope( 
                        name = "scopeName" , 
                        description = "A discription about scope"
                        [,anyName = anyValue]* 
                    ),
                    @AuthorizationScope(...)
                }
            ] | 
            [, apiName = "apiKey" , in = "query|header"] 
            [,anyName = anyValue]* 
        ),
        @AuthorizationsConfiguration(...)
    }
    [,anyName = anyValue]* // Swagger element "x-anyName" : "anyValue"
)
```

_@ServiceConfig_

| Ballerina Field | Type | Description | Swagger Field |
|---|:---:|---|---|
| host| string| Host name or IP of the Ballerina Service. | host |
| schemes | string[] | Transport protocol of the Ballerina Service.(http, https, ws, wss)| schemes |
| interface | string | Interface for Transport configuration. | N/A |
| authorizationsConfigurations | @AuthorizationsConfiguration[] | Authorization schema associated with the Ballerina Service | securityDefinitions |
| anyName | any | Extension fields. | `x-`anyName (Swagger extensions) |

_@AuthorizationsConfiguration_

| Ballerina Field | Type | Description | Swagger Field |
|---|:---:|---|---|
| name| string| **Required.** Name of the authorization schema definition.| securityDefinitions name |
| description | string| A Description about authorization schema. | description |
| type| string| **Required.** Type of the authorization schema.(basic,oauth2,..)| type |
| apiName | string| **Required, if type is apikey** Name of the header or query param | name |
| in| string| **Required, if type is apikey** Location of the API Key | in |
| flow| string| **Required, if type is oauth2** Flow used by OAuth2 schema. | flow |
| authorizationUrl| string| **Required, if type is oauth2** authorizationUrl of the OAuth2 endpoint| authorizationUrl |
| tokenUrl| string| **Required, if type is oauth2** tokenUrl of the OAuth2 endpoint | tokenUrl |
| authorizationScopes| @AuthorizationScope[] | **Required, if type is oauth2** OAuth2 scopes| scopes |
| anyName | any | Extension fields. | `x-`anyName (Swagger extensions) |

_@AuthorizationScope_

| Ballerina Field | Type | Description | Swagger Field |
|---|:---:|---|---|
| name| string| Name of the OAuth2 scope. | name|
| description | string| A description about the OAuth2 scope. | value of the name |
| anyName | any | Extension fields. | `x-`anyName (Swagger extensions) |

#### Path

Describes Base path of the HTTP Ballerina Service/API. This is a configuration annotation.

Syntax:
```
@Path("/context")
```

Value of the `@Path` annotation can't be empty and it should start with `/`. Swagger 2.0 equivalent field is `basePath`.

#### Consumes

Defines A list of MIME types the Ballerina Service can consume. This is global to all the resource defined within a 
  service. This is a configuration annotation.

```
@Consumes({ "MIME-type" [, "MIME-type"]*})
```

E.g: 
```
@Consumes({"application/json", "application/xml"})
```

#### Produces

Defines A list of MIME types the Ballerina Service can produce. This is global to all the resource defined within a 
  service. This is a configuration annotation.

```
@Produces({ "MIME-type" [, "MIME-type"]*})
```

E.g: 
```
@Produces({"application/json", "application/xml"})
```

### Resource Annotation.


Following are the resource level annotations.

* Path
* HTTP method
* Resource Config
* Consumes
* Produces
* Method Parameter Definition
* Resource Info
* Responses

#### Path

Describes sub path of the HTTP Ballerina resource. This is a configuration annotation.

Syntax:
```
@Path("/resource-path")
```

#### HTTP method

Defines the HTTP methods that support by the Ballerina resource. This is a configuration annotation.

Syntax:
```
(@GET | @POST | @PUT | @DELETE | @OPTIONS | @HEAD | @PATCH )* 
```

#### Resource Config

@ResourceConfig use to define transport protocol and the authorization details for the resource. This is a configuration annotation.

Syntax:
```
@ResourceConfig(
    schemes = {http, https},
    authorizations = {
        @Authorization(
            name = "authorizationConfigName" 
            [, scopes = { "scope1", "scope2" }]
        ),
        @Authorization(...)
    }
    [,anyName = anyValue]*
)
```

_@ResourceConfig_

|Ballerina Field |  Type | Description | Equivalent Swagger Field|
|---|---|---|---|
| schemes | string | The transfer protocol for the ballerina resource. Values can be "http", "https", "ws" or "wss". | schemas  |
| authorizations | @Authorization | A declaration of which authorizations configurations are applied for this resource. | security  |
| anyName | any | Extension fields. | `x-`anyName (Swagger extensions) |

_@Authorization_

|Ballerina Field |  Type | Description | Equivalent Swagger Field|
|---|---|---|---|
| name | string | Authorization name that specified in @AuthorizationsConfiguration section in the service level. | security  |
| scopes | string[] | Authorize scope for this resource that specified in @AuthorizationScope in the service level under above AuthorizationsConfiguration. | Values that define in @AuthorizationScope[] in service level  |

#### Consumes

Defines A list of MIME types the Ballerina resource can consume. This is a configuration annotation.

Syntax:
```
@Consumes({ "MIME-type" [, "MIME-type"]*})
```

E.g: 
```
@Consumes({"application/json", "application/xml"})
```

#### Produces

Defines A list of MIME types the Ballerina resource can produce. This is a configuration annotation.

Syntax:
```
@Produces({ "MIME-type" [, "MIME-type"]*})
```

E.g: 
```
@Produces({"application/json", "application/xml"})
```

#### Method Parameter Definition

Defines resource parameter mapping.

#####Query Param
Syntax:
```
QueryParam
@Path("/foo")
resourceName(message m, @QueryParam(name = "paramName", description = "A Description", required = true) string name, ...) {...}
```
```
@Path("/foo")
@ParametersInfo ({
    @ParamterInfo(
       id = "identifier",
       name = "paraName",
       description = "description about the paramter",
       required = true|false,
       type = "string" | "number" | "integer" | "boolean" | "array" | "file",
       format = "int32" | "int64" | "float" | "double" | "byte" | "binary" | "date" | "date-time" | "password",
       collectionFormat = "csv" | "ssv" | "tsv" | "pipes" | "multi"
    ),
    @ParamterInfo(...)
})
resourceName(message m, @QueryParam(name = "paramName") type identifier, ...) {...}
```

##### Path Param
Syntax:
```
PathParam
@Path("/foo/{paramName}")
resourceName(message m, @PathParam(name = "paramName", description = "A Description", required = true) string name, ...) {...}
```
```
@Path("/foo/{paramName}")
@ParametersInfo ({
    @ParamterInfo(
       id = "identifier",
       name = "paraName",
       description = "description about the paramter",
       required = true,
       type = "string" | "number" | "integer" | "boolean" | "array" | "file",
       format = "int32" | "int64" | "float" | "double" | "byte" | "binary" | "date" | "date-time" | "password",
       collectionFormat = "csv" | "ssv" | "tsv" | "pipes" | "multi"
    ),
    @ParamterInfo(...)
})
resourceName(message m, @PathParam(name = "paramName") type identifier, ...) {...}
```

##### Form Param
Syntax:
```
FormParam
@Path("/foo")
resourceName(message m, @FormParam(name = "paramName", description = "A Description", required = true) string name, ...) {...}
```
```
@Path("/foo")
@ParametersInfo ({
    @ParamterInfo(
       id = "identifier",
       name = "paraName",
       description = "description about the paramter",
       required = true|false,
       type = "string" | "number" | "integer" | "boolean" | "array" | "file",
       format = "int32" | "int64" | "float" | "double" | "byte" | "binary" | "date" | "date-time" | "password",
       collectionFormat = "csv" | "ssv" | "tsv" | "pipes" | "multi"
    ),
    @ParamterInfo(...)
})
resourceName(message m, @FormParam(name = "paramName") type identifier, ...) {...}
```

##### Header Param
Syntax:
```
HeaderParam
@Path("/foo")
resourceName(message m, @HeaderParam(name = "paramName", description = "A Description", required = true) string name, ...) {...}
```
```
@Path("/foo")
@ParametersInfo ({
    @ParamterInfo(
       id = "identifier",
       name = "paraName",
       description = "description about the paramter",
       required = true|false,
       type = "string" | "number" | "integer" | "boolean" | "array" | "file",
       format = "int32" | "int64" | "float" | "double" | "byte" | "binary" | "date" | "date-time" | "password",
       collectionFormat = "csv" | "ssv" | "tsv" | "pipes" | "multi"
    ),
    @ParamterInfo(...)
})
resourceName(message m, @HeaderParam(name = "paramName") type identifier, ...) {...}
```

##### Body
Syntax:
```
Body
@Path("/foo")
resourceName(message m, @Type(name = "TypeName", description = "A Description", required = true) TypeName typeVariableName, ...) {...}
```
```
@Path("/foo")
@ParametersInfo ({
    @ParamterInfo(
       id = "identifier",
       name = "paraName",
       description = "description about the paramter",
       required = true|false,
       schema = type
    ),
    @ParamterInfo(...)
})
resourceName(message m, type identifier, ...) {...}
```

#### Resource Info

A meta annotation which describes the meta details of this resource.

Syntax:
```
@ResourceInfo( 
    tags = { "http", "get", "anotherTag" },
    summary = "A summary about resource",
    description = "a detailed description about resource",
    externalDocs = @ExternalDocs(
                       description = "description", 
                       url = "..."
                   ),
    operationId = "Ballerina resource name"
)
```

_@ResourceInfo_

|Ballerina Field |  Type | Description | Swagger Field|
|---|---|---|---|
| tags| string[] | A list of tags for resource documentation control. | tags  |
| summary| string | A short summary of what the resource does. | summary  |
| externalDocs| @ExternalDocs | Additional external documentation for this resource. | externalDocs  |
| operationId| string | Unique string used to identify the resource (Ballerina resource name). | operationId  |

_@ExternalDocs_

|Ballerina Field | Type          | Description | Swagger Field
|---|---|---|---|
| description | string | a description about the target documentation. | description
| url | string | **Required.** URL is pointing to target documentation. | url
| anyName | any | Extension fields. | `x-`anyName (Swagger extensions) |


#### Responses

Defines an array of @Response which include possible responses that returns by this resource. 

Syntax:
```
@Responses({
    @Response(
        code = 200,
        [description = "Human-readable message to accompany the response.",
        response = TypeName|VaribaleType|ArrayType|XML<Schema>|JSON<Schema>
        headers = {
            @Header(
                name = "HeaderName",
                description = "description",
                type = TypeName|VaribaleType|ArrayType|XML<Schema>|JSON<Schema>
                ),
            @Header(...)
        },
        examples = {
            @Example(
                type = "mime-type",
                value = xml|json|string
            ),
            @Example(
                type = "application/json",
                value = `{ "key1": "value1", "key2", "value2"}`
            )
        }]
        | [reference = "String to reference"]
    ),
    @Response(
        code = "default",
        description = "unexpected error",
        ...)
})

```

_@Responses_

|Ballerina Field |  Type | Description | Swagger Field|
|---|---|---|---|
| Responses | @Response[] | **Required.** An array of possible @Response as they are returned from executing this operation. | responses  |

_@Response_

|Ballerina Field |  Type | Description | Swagger Field|
|---|---|---|---|
| code | int | **Required.** HTTP status code of this response  | Response HTTP status code  |
| description | string | A short description of the response. | description  |
| response | string | **Required.** The list of possible responses as they are returned from executing this operation  | schema  |
| headers | @Header[] | An array of @Header that are sent with the response  | headers  |
| examples | @Example[] | An @Example of the response message  | examples  |

_@Header_

|Ballerina Field |  Type | Description | Swagger Field|
|---|---|---|---|
| name | string | **Required.** Header name. | name of the header  |
| description | string | A short description of the header. | description  |
| type | string | **Required.** The type of the object. The value MUST be one of "string", "number", "integer", "boolean", or "array". | type  |


_@Example_

|Ballerina Field |  Type | Description | Swagger Field|
|---|---|---|---|
| type | string | **Required.** A supported mime type. | mime-tye value  |
| value | string | A sample response of this resource that match with given mime type. | The value SHOULD be an example of what such a response would look like  |


### Connector Annotations.

### Connection Annotations.

#### Circuit Breaker

Circuit Breaker prevents cascading failures in a connection.

Syntax: 
```
@CircuitBreaker(
    enable = true|false ,
    failureThreshold = integer ,
    initialOpenDuration = long ,
    maxOpenDuration = long ,
    openDurationFactor = float ,
    errorCodes = { string[] } , 
    criticalErrorCodes =  { string[] },  
    ignoredErrorCodes =  { string[] }
)
```

| Ballerina Field | Type | Description | 
|---|:---:|---|
| enable | boolean | Enable Circuit Breaker. Default is `false`.|
| failureThreshold | integer | Number of continuous failed messages to Open circuit. Default value is `3`. |
| initialOpenDuration | long | Circuit open duration in Milliseconds, before it moves to Half Open. Default value `60000.` |
| maxOpenDuration | long | Maximum Circuit open duration in Milliseconds. Default is `Long.max` |
| openDurationFactor | float >= 1.0 | Circuit open duration progression factor. Default is `1.0` |
| errorCodes | string[] | Error codes that are considered as connection failure and increment threshold value. If Threshold is reached, Open Circuit. By Default all error codes are enabled. |
| criticalErrorCodes | string[] | Error codes that are considered as critical connection failures, which Open Circuit immediately. By default, no error code is defined. |
| ignoredErrorCodes | string[] | Error codes that are not considered as connection failures, But that need to be dealt with as part of the integration logic. |

 
### Action Annotations. 
// TBD
### Function Annotations.
// TBD

### Type Annotations.

Defines user types.

Syntax:
```
type UserDefineType {
    @Property(
        name = "JSON_KEY", 
        required = true|false, 
        format = "int32" | "int64" | "float" | "double" | "byte" | "binary" | "date" | "date-time" | "password"
        [,anyName = anyValue]*
     )
    type variableName = DEFAULT_VALUE;
}
```

| Ballerina Field | Type | Description | Swagger Field |
|---|:---:|---|---|
| name | string | Identifier which user can use instead of the variable name. | UserDefineType or name |
| required | boolean | Whether is variable is required or not. | required |
| format | string | JSON/XML schema or data type. | format |
| anyName | any | Extension fields. | `x-`anyName (Swagger extensions) |

### Variable Annotations. 

Annotations that can be annotated to a Ballerina variable.

#### Property Annotation.

Property Annotation is used to describe meta information about the annotated variable field. Primary purpose of this 
 annotation is to provide rendering instruction for Ballerina tooling.
   
```
@Property(  schema = "name", 
            required = true|false, 
            format = "password|date|date-time|binary|byte"
)
```

| Ballerina Field | Type | Description | Swagger Field |
|---|:---:|---|---|
| schema | string | JSON/XML schema or data type. | Schema Object - $ref. |
| required | boolean | Indicate annotated field is required. Default is false.  | Schema Object -required. |
| title | string | Title for annotated field. | Schema Object -required. |
| description | string | A description about annotated variable. | Schema Object - description |
| format | string | DataType format (e.g: password, date, date-time, binary, byte, etc) | Schema Object - data type format. |
| * | string | Other Properties defined in the Swagger Schema Object.  | Schema Object - *. |


### Common Annotations

Annotations that can be annotated to any construct in the Ballerina.

#### Deprecated 

Denotes annotated Ballerina constructed is deprecated. This is a Doc annotation and **will not** produce any error during 
the compiler time or runtime of a Ballerina program. 

```
@Deprecated

or 

@Deprecated( since = "depricated version")
```
Swagger 2.0 defines `deprecate` field for resources.
