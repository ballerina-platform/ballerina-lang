# API Documentation Annotations

## Annotations

The following annotations are available in Ballerina for documenting APIs:

- @Description: 

    Use description annotation for explaining the purpose of functions, connectors, connector actions and structs.
       
- @Param:

    Use param annotation for explaining parameters.
    
- @Return:

    Use return annotation for explaining return parameters.

- @Field:

    Use field annotation for explaining fields of structs.
     
## Annotating Functions

Following is a sample function with documentation annotations:

````
@doc:Description("Add HTTP header to the message")
@doc:Param("m: Incoming message")
@doc:Param("key: HTTP header key")
@doc:Param("value: HTTP header value")
@doc:Return("result: Result of the execution")
public function addHeader (message m, string key, string value) (bool result) {
    system:println("invoked");
    return true;
}
````

## Annotating Connectors

Following is a sample connector with documentation annotations:

````
package foo.bar;

@doc:Description("Heapster Connector")
@doc:Param("clientSecret: Client secret")
@doc:Param("endpointUrl: Heapster endpoint URL")
@doc:Param("retryCount: Retry count")
connector TestConnector(string accessToken, string endpointUrl, int retryCount) {
    
    @doc:Description("Get CPU usage of a conatiner")
    @doc:Param("containerId: Id of the container")
    @doc:Return("cpu: CPU usage")
    action getCpuUsage(string containerId) (int cpuUsage) {
        // Find CPU usage of container
        return cpuUsage;
    }

    @doc:Description("Get Memory usage of a conatiner")
    @doc:Param("containerId: Id of the container")
    @doc:Return("memoryUsage: Memory usage")
    action getCpuUsage(string containerId) (int memoryUsage) {
        // Find memory usage of container
        return memoryUsage;
    }
}
````

## Annotating Structs

Following is a sample struct with documentation annotations:

````
package foo.bar;

@doc:Description("Inventory part definition")
@doc:Field("partId: Part ID")
@doc:Field("description: Part description")
@doc:Field("brand: Brand of the part")
struct InventoryPart {
    
    int partId;
    string description;
    string brand;
}
````