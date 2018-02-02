# API Documentation Annotations

## Annotations

The following annotations are available in Ballerina for documenting APIs:

- @doc:Description: 

    Use description annotation for explaining the purpose of services, resources, functions, connectors, connector actions, typemappers, structs, annotation definitions and constants.
       
- @doc:Param:

    Use param annotation for explaining parameters.
    
- @doc:Return:

    Use return annotation for explaining return parameters.

- @doc:Field:

    Use field annotation for explaining fields of structs.
     
## Annotating Functions

Following is a sample function with documentation annotations:

````
@doc:Description{value:"Add HTTP header to the message"}
@doc:Param{value:"m: Incoming message"}
@doc:Param{value:"key: HTTP header key"}
@doc:Param{value:"value: HTTP header value"}
@doc:Return{value:"result: Result of the execution"}
function addHeader (message m, string key, string value) (boolean result) {
    system:println("invoked");
    return true;
}
````

## Annotating Connectors

Following is a sample connector with documentation annotations:

````
package foo.bar;

import ballerina.doc;

@doc:Description{value:"Heapster Connector"}
@doc:Param{value:"accessToken: Access Token"}
@doc:Param{value:"endpointUrl: Heapster endpoint URL"}
@doc:Param{value:"retryCount: Retry count"}
connector HeapsterConnector(string accessToken, string endpointUrl, int retryCount) {

    @doc:Description{value:"Get CPU usage of a conatiner"}
    @doc:Param{value:"c: connector object"}
    @doc:Param{value:"containerId: Id of the container"}
    @doc:Return{value:"cpu: CPU usage"}
    action getCpuUsage(HeapsterConnector c, string containerId) (int cpuUsage) {
        // Find CPU usage of container
        return cpuUsage;
    }

    @doc:Description{value:"Get Memory usage of a conatiner"}
    @doc:Param{value:"c: connector object"}
    @doc:Param{value:"containerId: Id of the container"}
    @doc:Return{value:"memoryUsage: Memory usage"}
    action getCpuUsage(HeapsterConnector c, string containerId) (int memoryUsage) {
        // Find memory usage of container
        return memoryUsage;
    }
}
````

## Annotating Structs

Following is a sample struct with documentation annotations:

````
package foo.bar;

import ballerina.doc;

@doc:Description{value:"Inventory part definition"}
@doc:Field{value:"partId: Part ID"}
@doc:Field{value:"description: Part description"}
@doc:Field{value:"brand: Brand of the part"}
struct InventoryPart {
    
    int partId;
    string description;
    string brand;
}
````
