# Generating JSON Schema (Swagger 2.0 Compatible) and XML schema from Ballerina Type.

## Sample 1: Simple Type Convert.

XML and JSON type conversion without annotation. Describes default type mapping. 

* By default XML elements will considered as `unqualified`.

_Input Ballerina Sources_
* [./simple/person.bal](./simple/person.bal)

_Generated Outputs_ 
* Swagger 2.0 Definition - [./simple/person.yaml](./simple/person.yaml)
* XML Schema - [./simple/person.xsd](./simple/person.xsd)
* Sample XML - [./simple/person.xml](./simple/person.xml)


## Sample 2: Annotation based Type conversion. 

Same Sample 1 with Annotations. Annotations are used to fine tune, XML and JSON schema properties. 

* Once `XML namespace` attribute is defined, XML element will considered as qualified. All child will inherits same namespace. This can be override by defining another `XML namespace` attribute or `XML prefix`.  

_Input Ballerina Sources_
 * [./annotated/person.bal](./annotated/person.bal)

_Generated Outputs_ 
* Swagger 2.0 Definition - [./annotated/person.yaml](./annotated/person.yaml)
* XML Schema - [./annotated/person.xsd](./annotated/person.xsd)
* Sample XML - [./annotated/person.xml](./annotated/person.xml)