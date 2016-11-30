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
@AnnotationName [ ( PrimitiveValue | "AString"  | key = value [, key = value] )]
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
    
    @AnnotationFour( 
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

A Doc annotation, which describes Ballerina Annotation. 

```
@APIDefinition( // Alternatives : @Info, @ServiceInfo, @APIInfo, @APIDocumentation
    SwaggerVersion = "2.0", 
    info = @Info( // Swagger Info (Required)
        title = "Sample API title", // Swagger Info.title (Required)
        description =  "Sample Description.", // Swagger Info.Description 
        version = "1.0.0", // Swagger Info.Version (Required)
        termsOfService = "http://example.com/terms", // Swagger Info.TermsOfService
        contact = @Contact( // Swagger Info.contact
            name = "wso2" ,
            url = "http//wso2.com"
            [,anyName = anyValue]* // Swagger element "x-anyName" : "anyValue"
        ), 

        license = @License( // Swagger Info.license
            name = "Apache 2",
            url = "http://www.apache.org/licenses/LICENSE-2.0"
            [,anyName = anyValue]* // Swagger element "x-anyName" : "anyValue"
        ) 

        [,anyName = anyValue]* // Swagger element "x-anyName" : "anyValue"
    ),

    externalDocs = @ExternalDocs( // Swagger external Docs
        value = "wso2 ballerina",
        url = "http://docs.wso2.com/ballerina"
        [,anyName = anyValue]* // Swagger element "x-anyName" : "anyValue"
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

Ballerina Field Name  |  Description | Equivalent Swagger Field
----------------------|--------------|-------------------------
SwaggerVersion | Optional. Specifies the Swagger Specification version to be used. Default value is "2.0". | swagger 
info | Required Field. Provides metadata about the Ballerina API. | info
externalDocs |A list of tags used by the specification with additional metadata.| externalDocs
tags | Optional. A list of tags used by the specification with additional metadata. | tags

_@Info_

Ballerina Field Name  |  Description | Equivalent Swagger Field
----------------------|--------------|-------------------------
title||
description||
version||
termsOfService||
contact||
license||
anyName*||




#### API Configuration


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

