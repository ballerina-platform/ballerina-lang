## Module overview

This module provides utility methods for obtaining reflective information about the Ballerina runtime.

## Samples

### Check whether two objects are equal

The `reflect:equals` function checks whether two objects in the Ballerina runtime, including arrays, are equal. 

The samples below show how to compare primitive types. 

```ballerina
// Compare two strings.
string s1 = "a";
string s2 = "a";
boolean equal = reflect:equals(s1,s2); // Returns `true`

// Compare two string arrays.
string[] s1 = ["a", "b"];
string[] s2 = ["a", "b"];
boolean equalArrays = reflect:equals(s1,s2);  // Returns `true`
```
The sample below shows how to compare complex types.

```ballerina
// Compare two JSON objects.
json jObj1 = {   name:"Target",
                 location:{
                              address1:"19, sample road",
                              postalCode: 6789
                          },
                 products:[{price: 40.50, isNew: true, name:"apple"},
                           {name:"orange", price: 30.50}],
                 manager: ()
             };
json jObj2 = {   name:"Target",
                 location:{
                              address1:"19, sample road",
                              postalCode: 6789
                          },
                 products:[{price: 40.50, isNew: true, name:"apple"},
                           {name:"orange", price: 30.50}],
                 manager: ()
             };
boolean equalJsons = reflect:equals(jObj1,jObj2);
```

### Get service annotations

The sample below shows how to retrieve all the annotations of a service:

```ballerina
@http:ServiceConfig { basePath: "/helloWorld" }
service<http:Service> hello bind { port: 9090 } {
    hello(endpoint caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload("hello world");
        var result = caller->respond(res);
    }
}

reflect:annotationData[] annotations= reflect:getServiceAnnotations(hello); 
string annoName = annotations[0].name; //Eg. “ServiceConfig”
string annoPkg = annotations[0].moduleName; //Eg/ “ballerina.http”

```
