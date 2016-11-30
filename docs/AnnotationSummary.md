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
@AnnotationName [ ( PrimitiveValue | "AString" | Array | key = value [, key = value] )]
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
    
    @AnnotationThree( keyInt = 1 , keyString = "second value", keyAnnotation = @InnerAnnotation("Inner Annotation Value"))
    
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

## Swagger 2.0 Support

Ballerina Annotations represent a supper set of Open API Specification 2.0 (AKA Swagger 2.0) format. This allows developers
to Generate a Ballerina service skeleton from a Swagger 2.0 definition or Swagger 2.0 definition from a Ballerina service.
 
## Supported Annotations.

### Service Annotations. 

Followings are the service level annotations.

* API Definition
* API Configuration
* Path
* Consumes
* Produces

#### API Definition

A Doc annotation, which describes Ballerina Service. 

```
@APIDefinition( // Alternatives : @Info, @ServiceInfo, @APIInfo, @APIDocumentation
    swaggerVersion = "2.0", 
    info = @Info( 
        title = "Sample API title", 
        description =  "Sample Description.", 
        version = "1.0.0", 
        termsOfService = "http://example.com/terms",
        contact = @Contact(
            name = "wso2" ,
            url = "http//wso2.com"
            [,anyName = anyValue]* 
        ), 

        license = @License( 
            name = "Apache 2",
            url = "http://www.apache.org/licenses/LICENSE-2.0"
            [,anyName = anyValue]* 
        ) 

        [,anyName = anyValue]* 
    ),

    externalDocs = @ExternalDocs( 
        description = "wso2 ballerina",
        url = "http://docs.wso2.com/ballerina"
        [,anyName = anyValue]* 
    ),
    
    tags = { // Swagger tags element.
            @Tag( // Swagger tag element.
                name = "HTTP",
                description = "HTTP Service",
                externalDocs = @ExternalDocs(
                    value = "HTTP Service",
                    url = "http://docs.wso2.com/http"
                    [,anyName = anyValue]* // this will represent swagger element "x-anyName" : "anyValue"
                )
            ),
            @Tag(name = "Private", description = "Private Service")
        }
)
```

_@APIDefinition_

Ballerina Field | Type          | Description                                                       | Swagger Field
----------------|:-------------:|-------------------------------------------------------------------|-------------------
swaggerVersion  | string        | Specifies the Swagger Specification version to be used. Default value is "2.0". | swagger 
info            | annotation    | **Required.** Provides metadata about the Ballerina API.          | info
externalDocs    | annotation    | A list of tags used by the specification with additional metadata.|externalDocs
tags            | annotation[]  | A list of tags used by the specification with additional metadata.| tags

_@Info_

Ballerina Field | Type          | Description                                                       | Swagger Field
----------------|:-------------:|-------------------------------------------------------------------|-------------------
title           | string        | **Required.**  The title of the Ballerina Service.                | title
description     | string        | A description of the Ballerina Service.                           | description
version         | string        | **Required.** The version of the Ballerina Service.               | version
termsOfService  | string        | Text or URL for the Terms of Services for the Ballerina Service.  | termsOfService
contact         | annotation    | The Contact information for Ballerina Service.                    | contact 
license         | annotation    | The License information for Ballerina Service.                    | license
anyName         | any           | Extension fields.                                                 | `x-`anyName (Swagger extensions)

_@ExternalDocs_

Ballerina Field | Type          | Description                                                       | Swagger Field
----------------|:-------------:|-------------------------------------------------------------------|-------------------
description     | string        | a description about the target documentation.                     | description
url             | string        | **Required.** URL is pointing to target documentation.            | url
anyName         | any           | Extension fields.                                                 | `x-`anyName (Swagger extensions)

_@Tag_

Ballerina Field | Type          | Description                                                       | Swagger Field
----------------|:-------------:|-------------------------------------------------------------------|-------------------
name            | string        | **Required.** Name of tag.                                        | name
description     | string        | Description explaining current tag.                               | description
externalDocs    | annotation    | Additional external documentation link explaining current tag.    | url
anyName         | any           | Extension fields.                                                 | `x-`anyName (Swagger extensions)

_@Contact_

Ballerina Field | Type          | Description                                                       | Swagger Field
----------------|:-------------:|-------------------------------------------------------------------|-------------------
name            | string        | Name of the contact person or organization.                       |name
url             | string        | An URL pointing to contact information.                           |url
anyName         | any           | Extension fields.                                                 | `x-`anyName (Swagger extensions)

_@License_

Ballerina Field | Type          | Description                                                       | Swagger Field
----------------|:-------------:|-------------------------------------------------------------------|-------------------
name            | string        | Name of the License used for Ballerina Service.                   | name 
url             | string        | An URL  pointing to License information.                          | url
anyName         | any           | Extension fields.                                                 | `x-`anyName (Swagger extensions)

#### API Configuration

A Config annotation, which configure Ballerina Service. 

```
@APIConfiguration (
    host = "http://example.com/sample/service", // Swagger hosts
    schemes = {"http", "https"} // Swagger schemas
    authorizationsConfigurations = { // Swagger securityDefinitions
        @AuthorizationsConfiguration(
        name = "anUniqueName", 
        type = "basic|apiKey|oauth2|...", 
        description = ""
        // Swagger Oauth2 authentication.
        [, flow = "implicit|password|application|accessCode" , 
           authorizationUrl = "..." , 
           tokenUrl = "..." , 
           authorizationScopes = {
        @AuthorizationScope( name = "scopeName" , 
                             description = "A discription about scope"
                        [,anyName = anyValue]* // this will represent swagger element "x-anyName" : "anyValue"
        ),
        @AuthorizationScope(...)
        }] | // Swagger API KEY authentication. 
        [, apiName = "apiKey" , in = "query|header"] 
        
        
        [,anyName = anyValue]* // Swagger element "x-anyName" : "anyValue"
        ),
        @AuthorizationsConfiguration(...)
    }
    [,anyName = anyValue]* // Swagger element "x-anyName" : "anyValue"
)
```

#### Path


#### Consumes


#### Produces


### Resource Annotation.

### Connector Annotations.

### Connection Annotations.
 
### Action Annotations. 

### Function Annotations. 

### Type Annotations.

### Variable Annotations. 

