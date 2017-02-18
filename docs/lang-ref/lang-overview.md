# Language Overview

This page provides an overview of the Ballerina language and the main points you need to know about creating a Ballerina program. Be sure to read [Key Concepts](../key-concepts.md) to familiarize yourself with Ballerina entities such as services.

## Structure of a Ballerina program

A Ballerina file is structured as follows::

```
[package PackageName;]
[import PackageName [version ImportVersionNumber] [as Identifier];]*

(ServiceDefinition |
 FunctionDefinition |
 ConnectorDefinition |
 TypeDefinition |
 TypeMapperDefinition |
 ConstantDefinition)+
```

Note: Terminals of the language (keywords) are lowercase, whereas non-terminals are uppercase.

Each of the Ballerina entities such as services and connectors are described in detail in their own pages in this guide.

A Ballerina program can consist of a number of Ballerina files, which may be in one or more packages. Ballerina uses a modular approach for managing names and organizing code into files. In summary, Ballerina entities (functions, services, etc.) all have globally unique qualified names consisting of their package name and the entity name. For complete information, see [Packaging and Running Programs](packaging.md).

## Expressions
Similar to languages such as Java, Go, etc, Ballerina supports the following expressions: 

* Mathematical expressions `(x + y, x/y, etc.)`
* Function calls `(foo(a,b))`
* Action calls `(tweet(twitterActor, "hello"))`
* Complex expressions `(foo(a,bar(c,d)))`

## Reserved names

When naming your Ballerina elements (services, resources, actions, etc.) and packages, do not use the following terms for the name, as these terms are key words or otherwise reserved in Ballerina:

action
all
any
as
boolean
break
catch
connector
const
datatable
double
else
exception
false
fork
function
if
import
int 
json
map
message
native
null
package
reply
resource
return
service
string
struct
throws
timeout
true
try
typemapper
while
worker
xml

## Documenting your code

As you develop new connectors, actions, and functions that you want to share with others, it's important to add documentation that describes each entity and how it's used. Ballerina provides a documentation framework called Docerina that generates API documentation from your annotations in your Ballerina files. You can check it out at https://github.com/ballerinalang/docerina. 

## Testing your code

When you write your code in Ballerina Composer, the syntax is checked for you as you write it. You can also manually test a Ballerina file using the following command:

```
ballerina test <ballerina_file>
```
Ballerina provides a testing framework called Testerina that you can use for your programs. You can check it out at https://github.com/ballerinalang/testerina. 
