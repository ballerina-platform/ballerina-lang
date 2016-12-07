# Introduction to Ballerina Annotations. 

## Introductions

Ballerina `Annotations` are from of metadata, that attached to a Ballerina construct. By design, Ballerina considers all
annotations as optional, hence A Ballerina Program(Service ?) should be able to run without any annotation. 

Ballerina `Annotations` can be used to annotate following Ballerina constructs.   
 
 * Service
 * Resource
 * Connectors
 * Actions
 * Functions
 * User Defined Types
 * Variables
 

All Ballerina Annotations start with Character `@` (at) and it has following Syntax. 

```java
@AnnotationName [ ( PrimitiveValue | "A String" | Array | key = value [, key = value] )]
```

Here `value` can be one of the followings:
* Primitive value. (i.e. integer, long, float, double, boolean)
* A String. (e.g. "aValue")
* Another Annotation. (This can't be the parent or enclosing annotation)
* An Array of one of the above. (e.g. { "String 1", "String 2" })

E.g:
```java
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

* Documentation Annotations (Doc/Meta Annotation)
    - These annotations represent structured meta information about the Ballerina constructs that they annotated.(Similar to
     Java Doc comment.) 
* Config Annotations
    - Config annotations denote additional configuration/behavior instructions for Ballerina runtime.
     (e.g Apply Security, Circuit Breaker, etc) By defining the config annotations for a construct, Ballerina developer 
     can instruct to the Ballerina runtime, to alter default behavior or/and apply Quality of services configuration.
     
Note: By design Doc annotation and Config annotation are defined separately. This will improve readability and performance
 of the system.  

## Open API 2.0 (Swagger 2.0) Support in Ballerina. 

Ballerina Annotations represent a super set of Open API Specification 2.0 (AKA Swagger 2.0) format. This allows developers
to Generate a Ballerina service skeleton from a Swagger 2.0 definition and/or Swagger 2.0 definition from a Ballerina service.


## Supported Annotations.

* [Service Annotations](#service-annotations)
* [Recourse Annotations](#resource-annotation)
* [Connectors and Action Annotations.](#connector-annotations) 
* [Type and Variable Annotations.](#type-and-variable-annotations)
* [Function Annotations.](#function-annotations)
* [Common Annotations.](#common-annotations)


## Example Index

 * Service Annotations - 
 * Generating Ballerina using Swagger 2.0 Definition 
    - [Pet Store Sample](../sampels/annotation/petstore/)
 * Type Mapping
    - [Generating JSON Schema (Swagger 2.0 compliant) and XML Schema.](../samples/annotation/type/fromBallerina)
    - [Generating Ballerina Types using Swagger 2.0](../samples/annotation/type/fromSwagger)
    

## Service Annotations. 

Followings are the service level annotations.

* Service Info
* Service Config
* Path
* Consumes
* Produces

### Service Info

A Doc annotation which describes a Ballerina Service. 

Generic meta information about Service. This a super set of Swagger 2.0 representation. 

Syntax: 
```java
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
    
    resource testResource(message m){
        // Do something.
    }
}
```

##### _@ServiceInfo_

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

##### _@Contact_

| Ballerina Field | Type | Description | Swagger Field (Json Path) |
|---|:---:|---|---|
| name | string | Name of the contact person or organization. | $.info.contact.name |
| email | string | email of the contact person or organization. | $.info.contact.x-email |
| url | string | An URL pointing to contact information. | $.info.contact.url |

##### _@License_

| Ballerina Field | Type | Description | Swagger Field |
|---|:---:|---|---|
| name | string | Name of the License used for Ballerina Service. | $.info.license.name |
| url | string | A URL pointing to License information. | $.info.license.url |

##### _@ExternalDoc_

| Ballerina Field | Type | Description | Swagger Field |
|---|:---:|---|---|
| description | string | a description about the target documentation. | parent.description |
| url | string | **Required.** URL is pointing to target documentation. | parent.url |

##### _@Tag_

| Ballerina Field | Type | Description | Swagger Field |
|---|:---:|---|---|
| name | string | **Required.** Name of tag. | $.tags\[position\].name |
| description | string | Description explaining current tag. | $.tags\[position\].description |
| doc | @Doc | Additional external documentation link explaining current tag. | $.tags\[position\].externalDocs |

##### _@Organization_

| Ballerina Field | Type | Description | Swagger Field |
|---|:---:|---|---|
| name | string | Name of the Organization. | $.info.x-organization.name |
| url | string | An URL pointing to the Organization website. | $.info.x-organization.url |

##### _@Developer_

| Ballerina Field | Type | Description | Swagger Field |
|---|:---:|---|---|
| name | string | Name of the developer. | $.info.x-developers\[position\].name |
| email | string | An email address of the Developer. | $.info.x-developers\[position\].email |

##### _@Swagger_
 
This annotation represents all the vendor specific custom swagger extensions. 

**Note** `@Swagger` can be used only to annotate Ballerina Service, Resource, and Type fields. 

| Ballerina Field | Type | Description | Swagger Field (JSON Path) |
|---|:---:|---|---|
| version | string | **Required.** Specifies the Swagger Specification version to be used. If `@Swagger` annotation is not defined, default value is "2.0" | $.swagger |
| extension | @SwaggerExtension[] | List of Swagger extension for specific swagger field. | any |

##### _@SwaggerExtension_

| Ballerina Field | Type | Description | Swagger Field (JSON Path) |
|---|:---:|---|---|
| target | string | **Required.** JSON path of the swagger field. | target |
| anyKey | any | List of Swagger extension for a specific swagger field. | target.x-anykey |


### Service Config

A Config annotation, which represents common configuration for a Ballerina Service. 

Syntax:
```java
@ServiceConfig (
    host = "http://example.com/sample/service" , 
    schemes = {"http", "https"} ,
    interface = "Interface Key or path to Interface." 
    authorizations = { 
        @Authorization(
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
        ),
        @Authorization(...)
    }
)
```

##### _@ServiceConfig_

| Ballerina Field | Type | Description | Swagger Field (JSON Path) |
|---|:---:|---|---|
| host| string| Hostname or IP of the Ballerina Service. | $.host |
| schemes | string[] | Transport protocol of the Ballerina Service.(http, https, ws, wss)| $.schemes |
| interface | string | Interface for Transport configuration. | N/A |
| authorizations | @Authorization[] | Authorization schema associated with the Ballerina Service | $.securityDefinitions |

##### _@Authorization_

| Ballerina Field | Type | Description | Swagger Field (Json Path) |
|---|:---:|---|---|
| name| string | **Required.** Name of the authorization schema definition.| json_name($.securityDefinitions\[*\]\['name'\]) |
| description | string | A Description about authorization schema. | $.securityDefinitions\[*\]\['name'\].description |
| type| string | **Required.** Type of the authorization schema.(basic,oauth2,..)| $.securityDefinitions\[*\]\['name'\].type |
| apiName | string | **Required, if type is apikey** Name of the header or query param | $.securityDefinitions\[*\]\['name'\].name |
| in | string | **Required, if type is apikey** Location of the API Key | $.securityDefinitions\[*\]\['name'\].in |
| flow | string | **Required, if type is oauth2** Flow used by OAuth2 schema. | $.securityDefinitions\[*\]\['name'\].flow |
| authorizationUrl | string | **Required, if type is oauth2** authorizationUrl of the OAuth2 endpoint| $.securityDefinitions\[*\]\['name'\].authorizationUrl |
| tokenUrl | string | **Required, if type is oauth2** tokenUrl of the OAuth2 endpoint | $.securityDefinitions\[*\]\['name'\].tokenUrl |
| authorizationScopes| @AuthorizationScope[] | **Required, if type is oauth2** OAuth2 scopes| $.securityDefinitions\[*\]\['name'\].scopes |

##### _@AuthorizationScope_

| Ballerina Field | Type | Description | Swagger Field (Json Path) |
|---|:---:|---|---|
| name| string| Name of the OAuth2 scope. | json_name($.securityDefinitions\[*\]\['name'\].scopes.\['name'\])|
| description | string| A description about the OAuth2 scope. | $.securityDefinitions\[*\]\['name'\].scopes.\['name'\] |

### Path

Describes Base path of the HTTP Ballerina Service/API. This is a configuration annotation.

Syntax:
```java
@Path("/context")
```

The value of the `@Path` annotation can't be empty and it should start with `/`. This is an Optional annotation and default
service context is mapped to `/`

Swagger 2.0 equivalent field is `basePath`. (Json Path `$.basePath`)

### Consumes

Defines A list of MIME types the Ballerina Service can consume. This is global to all the resource defined within a 
  service. This is a configuration annotation.

```java
@Consumes({ "MIME-type" [, "MIME-type"]*})
```

E.g: 
```java
@Consumes({"application/json", "application/xml"})
```

This is an optional annotation. Swagger 2.0 equivalent field is `Consumes`. (JSON Path `$.consumes`)

### Produces

Defines A list of MIME types the Ballerina Service can produce. This is global to all the resource defined within a 
  service. This is a configuration annotation.

```java
@Produces({ "MIME-type" [, "MIME-type"]*})
```

E.g: 
```java
@Produces({"application/json", "application/xml"})
```

This is an optional annotation. Swagger 2.0 equivalent field is `produces`. (JSON Path `$.produces`)

## Resource Annotation.

Following are the resource level annotations.

* Path
* HTTP method
* Resource Config
* Consumes
* Produces
* Method Parameter Definition
* Resource Info
* Responses

### Path

Describes sub path of the HTTP Ballerina resource. This is a configuration annotation.

Syntax:
```java
@Path("/resource-path")
```

The value of the `@Path` annotation can't be empty and it should start with `/`.  This is a **Required** annotation for HTTP
Ballerina services. 

Swagger 2.0 equivalent field is `paths.path`. (Json Path `$.paths./resource-path`)

### HTTP method

Defines the HTTP methods that support by the Ballerina resource. This is a configuration annotation.

Syntax:
```java
(@GET | @POST | @PUT | @DELETE | @OPTIONS | @HEAD | @PATCH )* 
```

Swagger 2.0 equivalent field is `paths.path.HTTP_METHOD`. (Json Path `$.paths./resource-path.HTTP_METHOD`) Here 
HTTP_METHOD is either get, post, put, delete, option, head or patch.

**Note**: Typical Ballerina resource can have multiple HTTP methods associated with it. This feature is not supported by
 Swagger 2.0 specification. Hence this will map to multiple Swagger Operation Objects per path.
 
 E.g. Both put and post HTTP methods are mapped to ballerina resource `createStdRecord`.  
 ```java
 @ServiceInfo(
    title = "Student Management Service"
    version = "1.0.0"
 )
 service StudentManagementService{
    ...
    @Path("/createStudentRecord")
    @PUT
    @POST
    resource createStdRecord (message m)  {
       // Do something.
       reply m;
    }
    ...
 }
 ```
 
Equivalent Swagger definition. 
 
```yaml
swagger: '2.0'
info:
  version: '1.0.0'
  title: Student Management Service
paths:
  /createStudentRecord:
    put:
      operationId: createStdRecord_put
      responses:
        default:
          description: Default Response.
          schema: 
            $ref: '#/ballerina/default/response'
    post:
      operationId: createStdRecord_post
      responses:
        default:
          description: Default Response.
          schema:
            $ref: '#/ballerina/default/response'
```

Swagger operationId has format `BallerinaResourceName_httpMethod`. 


### Resource Config

`@ResourceConfig` use to define transport protocol and the authorization details for the resource. This is a configuration annotation.

Syntax:
```java
@ResourceConfig(
    schemes = {http, https},
    authorizations = {
        @Authorization(
            name = "authorizationConfigName" 
            [, scopes = { "scope1", "scope2" }]
        ),
        @Authorization(...)
    }
)
```

##### _@ResourceConfig_

|Ballerina Field |  Type | Description | Swagger Field (Json Path) |
|---|---|---|---|
| schemes | string[] | The transfer protocol for the ballerina resource. Values can be "http", "https", "ws" or "wss". | $.paths./resource-path.schemas  |
| authorizations | @Authorization[] | A declaration of which authorizations configurations are applied for this resource. | $.paths./resource-path.security  |

##### _@Authorization_

|Ballerina Field |  Type | Description | Swagger Field (Json Path) |
|---|---|---|---|
| name | string | Authorization name that specified in @AuthorizationsConfiguration section in the service level. | json_name($.paths./resource-path.security\[*\]\["name"\])  |
| scopes | string[] | Authorize scope for this resource that specified in @AuthorizationScope in the service level under above AuthorizationsConfiguration. | $.paths./resource-path.security\[*\]\["name"\], (Values that define in @AuthorizationScope[] in service level)  |

### Consumes

Defines A list of MIME types the Ballerina resource can consume. This is a configuration annotation.

Syntax:
```java
@Consumes({ "MIME-type" [, "MIME-type"]*})
```

E.g: 
```java
@Consumes({"application/json", "application/xml"})
```
This is an optional annotation. Swagger 2.0 equivalent field is `consumes`. (Json Path `$.paths./resource-path.consumes`)

### Produces

Defines A list of MIME types the Ballerina resource can produce. This is a configuration annotation.

Syntax:
```java
@Produces({ "MIME-type" [, "MIME-type"]*})
```

E.g: 
```java
@Produces({"application/json", "application/xml"})
```

This is an optional annotation. Swagger 2.0 equivalent field is `produces`. (JSON Path `$.paths./resource-path.produces`)

### Method Parameter Definition

Defines resource parameter mapping.

#### Query Param

Represents an HTTP Query Param of the annotated resource. 

Syntax:
```java
@Path("/foo")
resourceName(message m, @QueryParam("paramName") type identifier, ...) {...}
``` 

`paramName` is **Required** and Case Sensitive. It should match to Query Param of the URL and can't be an empty string.

#### Path Param

Represents a Path Param of the annotated resource.

Syntax:
```java
@Path("/foo/{paramName}")
resourceName(message m, @PathParam("paramName") type identifier, ...) {...}
```

`paramName` is **Required** and Case Sensitive. It should match to Path Param of the URL which is defined in `@Path` 
annotation. It can't be an empty string.

#### Form Param

Represents an HTTP Form field of the annotated resource. This annotation applies only for content type `application/x-www-form-urlencoded` or `multipart/form-data` 

Syntax:
```java
@Path("/foo")
resourceName(message m, @FormParam("paramName") string|byte identifier, ...) {...}
```
`paramName` is **Required** and Case Sensitive. It should match to Form Param (Described bellow) and can't be an empty string.

if content type is `application/x-www-form-urlencoded` - `paramName` is similar to the format of Query parameters but in a payload. 

If content type is `multipart/form-data` - value of the `name` parameter of the HTTP `Content-Disposition: form-data;` header. 

#### Header Param

Represents a transport header that is expected as part of the request.

Syntax:
```java
@Path("/foo")
resourceName(message m, @HeaderParam("paramName") string identifier, ...) {...}
```
`paramName` is **Required** and Case Sensitive. It should match to transport header name and can't be an empty string.

#### Body

Represents payload of the incoming request.

Syntax:
```java
@Path("/foo")
resourceName(message m, @Body("name") type identifier, ...) {...}
```

Since there is only one payload for a request, `@Body` annotation can be used once per resource. Form parameters 
 are defined in the payload, a resource definition can't have both `@Body` annotation or `@FormParam` annotations together. 
  
  `name` is **Required** and Case Sensitive. It can't be an empty string.


#### ParametersInfo (or Parameters)

ParametersInfo describes meta information about operation parameters (as described above) of a resource. `@ParametersInfo` contains one or more `@ParameterInfo` annotations.

Syntax:
```java
@ParametersInfo ({
    @ParameterInfo(
       in = "query" | "header" | "path" | "formData" | "body",
       name = "paraName",
       description = "description about the parameter",
       required = true|false,
       allowEmptyValue = true|false,
       type = "string" | "number" | "integer" | "boolean" | "array" | "file" ,
       format = "int32" | "int64" | "float" | "double" | "byte" | "binary" | "date" | "date-time" | "password",
       schema = typeName,
       collectionFormat = "csv" | "ssv" | "tsv" | "pipes" | "multi"
       items =  @items (
            type = "string" | "number" | "integer" | "boolean" | "array" | "file" ,
            format = "int32" | "int64" | "float" | "double" | "byte" | "binary" | "date" | "date-time" | "password",
            collectionFormat = "csv" | "ssv" | "tsv" | "pipes" | "multi"
            items = @Item(...)
       )
    ),
    @ParamterInfo(...)
})
```

##### _@ParameterInfo_ (or parameter)

|Ballerina Field |  Type | Description | Swagger Field (Json Path) |
|---|---|---|---|
| in | string | The location of the parameter. Possible values are "query", "path", "formData", "header" or "body". **Optional** If the Parameter is defined as a parameter of the resource definition using `@QueryParam`, `@PathParam`, `@FormParam`, `@HeaderParam`, or `@Body`. Otherwise this is **Required** and considered this as implicit meta information. | $.paths./resource-path.parameters\[position\].in  |
| name | string | **Required.** The name of the parameter which is defined in `@QueryParam`, `@PathParam`, `@FormParam` or `@HeaderParam` annotations.| $.paths./resource-path.parameters\[position\].name  |
| description | string | A short description about the parameter. | $.paths./resource-path.parameters\[position\].description  |
| required | string | Define whether this parameter is mandatory. Default is `true` for `@PathParam`, `false` otherwise. | $.paths./resource-path.parameters\[position\].required  |
| allowEmptyValue | string | Define empty values are supported. Valid only for either `query` or `formData` | $.paths./resource-path.parameters\[position\].allowEmptyValue  |
| type | string | Type of the parameter. **Optional** only if the Parameter is defined as a parameter of the resource definition using `@QueryParam`, `@PathParam`, `@FormParam` or `@HeaderParam`.  | $.paths./resource-path.parameters\[position\].type  |
| format | string | Format of the `type`.  | $.paths./resource-path.parameters\[position\].format  |
| schema | anyType |  Defines Payload format. Applies only if in is `body` or `@Body` annotation is defined.  **Required** if `@Body` is not defined.| $.paths./resource-path.parameters\[position\].schema  |
| collectionFormat | string | Determines the format of the array if type array is used. Default is `cvs`| $.paths./resource-path.parameters\[position\].collectionFormat  |
| items | @Items | **Required.** if `type` is an Array. Describes the items in the array. | $.paths./resource-path.parameters\[position\].items  |

##### _@Item_
|Ballerina Field |  Type | Description | Equivalent Swagger Field|
|---|---|---|---|
| type | string | **Required.** The internal type of the array. | $.paths./resource-path.parameters\[position\].items.type  |
| format | string | Format of the `type`. | $.paths./resource-path.parameters\[position\].items.format  |
| collectionFormat | string |  Determines the format of the array if type array is used. Default is `cvs` | $.paths./resource-path.parameters\[position\].items.collectionFormat  |
| items | string |  **Required.** if `type` is an Array. Describes the items in the array. | $.paths./resource-path.parameters\[position\].items.items  |


### Resource Info

A meta-annotation which describes the meta details of this resource.

Syntax:
```java
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

##### _@ResourceInfo_

|Ballerina Field | Type | Description | Swagger Field (Json Path) |
|---|---|---|---|
| tags| string[] | A list of tags for resource documentation control. | $.paths./resource-path.tags  |
| summary| string | A short summary of what the resource does. | $.paths./resource-path.summary  |
| description| string | A description about of what the resource does. | $.paths./resource-path.summary  |
| externalDocs| @ExternalDocs | Additional external documentation for this resource. | $.paths./resource-path.externalDocs  |
| operationId| string | Unique string used to identify the resource (Ballerina resource name). | $.paths./resource-path.operationId  |


### Responses

Defines an array of @Response which include possible responses that returns by this resource. 

Syntax:
```java
@Responses({
    @Response(
        code = 200,
        description = "Human-readable message to accompany the response.",
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

##### _@Responses_

|Ballerina Field |  Type | Description | Swagger Field (Json Path) |
|---|---|---|---|
| Responses | @Response[] | **Required.** An array of possible @Response as they are returned from executing this operation. |  $.paths./resource-path.responses  |

##### _@Response_

|Ballerina Field |  Type | Description | Swagger Field (Json Path) |
|---|---|---|---|
| code | int | **Required.** HTTP status code of this response  | json_name($.paths./resource-path.responses\[position\]["code"\])  |
| description | string | A short description of the response. | $.paths./resource-path.responses\[*\]\["code"\].description  |
| response | string | **Required.** The list of possible responses as they are returned from executing this operation  | $.paths./resource-path.responses\[*\]\["code"\].schema  |
| headers | @Header[] | An array of @Header that are sent with the response  | $.paths./resource-path.responses\[*\]\["code"\].headers  |
| examples | @Example[] | An @Example of the response message  | $.paths./resource-path.responses\[*\]\["code"\].examples  |

##### _@Header_

|Ballerina Field |  Type | Description | Swagger Field (Json Path) |
|---|---|---|---|
| name | string | **Required.** Header name. | json_name($.paths./resource-path.responses\[\*\]\["code"\].headers\[\*\]\["name"\])  |
| description | string | A short description of the header. | $.paths./resource-path.responses\[\*\]\["code"\].headers\[\*\]\["name"\].description  |
| type | string | **Required.** The type of the object. The value MUST be one of "string", "number", "integer", "boolean", or "array". | $.paths./resource-path.responses\[\*\]\["code"\].headers\[\*\]\["name"\].type  |


##### _@Example_

|Ballerina Field |  Type | Description | Swagger Field (Json Path) |
|---|---|---|---|
| type | string | **Required.** A supported mime type. | json_name($.paths./resource-path.responses\[*\]\["code"\].examples\[\*\]\['type'\])  |
| value | string | A sample response of this resource that match with given mime type.  The value SHOULD be an example of what such a response would look like  | $.paths./resource-path.responses\[*\]\["code"\].examples\[\*\]\['type'\] |


### Connector Annotations.

In Ballerina, A Connector (Based on HTTP) can be generated by importing a Swagger Definition. The connector has `@ConnectorInfo` annotation which shares same attribute set of the `@ServiceInfo` annotation and `@ConnectorConfig` annotation which share same attribute set of the `@ServiceConfig`. 

#### Circuit Breaker

The Circuit Breaker is a built in feature in Ballerina, that prevents cascading failures in a connector. This annotation applies only to connector instances. 

Syntax: 
```java
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
| failureThreshold | integer | Number of continuously failed messages to the Open circuit. The default value is `3`. |
| initialOpenDuration | long | Circuit open duration in Milliseconds, before it moves to Half Open. Default value `60000.` |
| maxOpenDuration | long | Maximum Circuit open duration in Milliseconds. Default is `Long.max` |
| openDurationFactor | float >= 1.0 | Circuit open duration progression factor. Default is `1.0` |
| errorCodes | string[] | Error codes that are considered as connection failure and increment threshold value. If Threshold is reached, Open Circuit. By Default all error codes are enabled. |
| criticalErrorCodes | string[] | Error codes that are considered as critical connection failures, which Open Circuit immediately. By default, no error code is defined. |
| ignoredErrorCodes | string[] | Error codes that are not considered as connection failures, But that need to be dealt with as part of the integration logic. |

 
#### Action Annotations. 

TODO: Following Section needs some re-thinking whether we should use the same format for action and resource annotation.  

Action can have @Doc annotation which describes the parameters and the return types. If Actions are generated using a swagger definition, each Swagger Operation Object will be mapped to the action.

|Action Annotation | Matching Resource Annotation | Description |
|---|---|---|
|ConnectorConfig|ResourceConfig| ??? |
|Consumes|Consumes| ??? |
|Produces|Produces| ??? |
|ParametersInfo|ParametersInfo| ??? |
|ConnectorInfo|ResourceInfo| ??? |

Syntax:
```java
@Doc(
    description = "description about the action",
    params = {
        @param(
            name = "name of the parameter",
            type = data type of the parameter,
            description = "description about the parameter"            
        )  
    },
    returns = {
        @return(
           type = data type of the return value,
           description = "This returns String array."
        ),
        @return(
           type = exception,
           description = "Returns exception if connection timeout."
        )
    }
)
```
| Ballerina Field | Type | Description | 
|---|:---:|---|
| description | string | Description about this action|
| param | @param[] | Array of @param that this actor accept.|
| return | @return[] | details about return types including possible exception situations.|


### Function Annotations.

The function can have @Doc annotation which describes the parameters and the return types.

Syntax:
```java
@Doc(
    description = "description about the function",
    params = {
        @param(
            name = "name of the parameter",
            type = data type of the parameter,
            description = "description about the parameter"            
        )  
    },
    returns = {
        @return(
           type = data type of the return value,
           description = "This returns String array."
        ),
        @return(
           type = exception,
           description = "Returns exception if connection timeout."
        )
    }
)
```
| Ballerina Field | Type | Description | 
|---|:---:|---|
| description | string | Description about this function|
| param | @param[] | Array of @param that this actor accept.|
| return | @return[] | details about return types including possible exception situations.|


## Type and Variable Annotations.


| Annotation | target | Description |
|---|---|---|
|@TypeInfo| Any type |Describes meta information about a Ballerina type. |
|@Property| Any variable (primitive or type variable) in a Type, Service, Connector | Describes meta information about a Ballerina type variable. |
|@Xml| - Any type<br/>- variable (primitive or type variable) in a Type  | Describes additional information about the XML representation of a Ballerina type or variable.  |

Syntax:
```java
@TypeInfo( 
        name = "userDefineType" ,
        title = "A title of the Type" ,
        description = "A Description about type"
)
@Xml(
    name = "xmlElementName" ,
    namespace = "https://example.com/foo" ,
    prefix = "foo" ,
    wrapped = true | false ,
    xsdType = "xmlElementType"
)
type UserDefineType {

    @Property(  
        name = "varName", 
        required = true|false, 
        title = "A title for this variable", 
        discription = "A short description about the variable field."
        format = "password|date|date-time|binary|byte"
    )
    @Xml(
        name = "InnerElementName" ,
        namespace = "https://example.com/bar" ,
        prefix = "bar" ,
        attribute = true | false ,
        xsdType = "InnerElementType"
    )
    type variableName = DEFAULT_VALUE;
}
```

##### _@TypeInfo_

| Ballerina Field | Type | Description |  Swagger Field (Json Path) |
|---|:---:|---|---|
| name | string | Identifier Swagger definition name. | json_name($.definitions\['userDefineType'\]) |
| title | string | Title for annotated type. |$.definitions\['userDefineType'\].title |
| description | string | A description about annotated type. | $.definitions\['userDefineType'\].description |

##### _@Property_

Describe meta information about the annotated variable or type field of a Type, Service, or Connector. Primary purpose of this 
 annotation is to provide rendering instruction for Ballerina tooling.

| Ballerina Field | Type | Description | Swagger Field (JSON Path)  |
|---|:---:|---|---|
| name | string | **Optional** filed to override Swagger filed name. Default name is taken from Ballerina variable name. | json_name($.definitions\['userDefineType'\].properties.\['varName'\]) |
| required | boolean | Indicate annotated field is required. The default is false.  | insert varName into ($.definitions\['userDefineType'\].required) |
| title | string | Title for annotated field. |$.definitions\['userDefineType'\].properties.\['varName'\].title |
| description | string | A description about annotated variable. | $.definitions\['userDefineType'\].properties.\['varName'\].description |
| format | string | Data Type format (e.g: password, date, date-time, binary, byte, etc) | $.definitions\['userDefineType'\].properties.\['varName'\].format |
| * | string | Other Properties defined in the Swagger Schema Object. https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#schemaObject  | .definitions\['userDefineType'\].properties.\['varName'\].* |

##### _@Xml_
   
| Ballerina Field | Type | Description | Swagger Field (JSON Path)  |
|---|:---:|---|---|
|name|string| Equivalent XML element name| **Variable :**  $.definitions\['userDefineType'\].properties.\['varName'\].xml.name  <br/>  **Type :**  $.definitions\['userDefineType'\].xml.name|
|namespace|string| Namespace of the XML element.| **Variable :** $.definitions\['userDefineType'\].properties.\['varName'\].xml.namespace  <br/> **Type :** $.definitions\['userDefineType'\].xml.namespace|
|prefix|string| Namespace prefix of the XML element.| **Variable :** $.definitions\['userDefineType'\].properties.\['varName'\].xml.prefix <br/> **Type :** $.definitions\['userDefineType'\].xml.prefix|
|attribute|boolean| **Only for Variables** If true, annotated variable considers as an attribute of the type. | **Variable :** $.definitions\['userDefineType'\].properties.\['varName'\].xml.attribute |
|wrapped|boolean| **Only for Type** MAY be used only for an array definition. Added this to comply with Swagger 2.0 | **Type :** $.definitions\['userDefineType'\].xml.wrapped |
|xsdType|string| Field specify XSD type name, `namespace` field is required. | **Variable :** $.definitions\['userDefineType'\].properties.\['varName'\].xml.x-xsdType <br/> **Type :** $.definitions\['userDefineType'\].xml.x-xsdType|

### Ballerina Variable vs Swagger Type & Format Mapping. 

| Ballerina type | Swagger Type | Swagger Format |
|---|---|---|
| int | integer | int32 |
| long | integer | int64 |
| float | number | float |
| double | number | double |
| boolean | boolean | N/A | 
| string | string | N/A | 
| string* | string | date, date-time, email, password | 
| (?)| string | binary | 
| (?)| string | byte | 

\* Using `@Proprty` annotations, string field can be converted in to date, date-time, email, or password field in Ballerina UI.    


Future Work: 

* Handle Swagger polymorphism in data type. - https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#models-with-polymorphism-support
 

### Common Annotations

Annotations that can be annotated to any construct in the Ballerina.

#### Deprecated 

Denotes annotated Ballerina constructed is deprecated. This is a Doc annotation and **will not** produce any error during 
the compile time or runtime of a Ballerina program. 

```
@Deprecated

or 

@Deprecated( since = "depricated version")
```
Swagger 2.0 defines `deprecate` field for resources.
