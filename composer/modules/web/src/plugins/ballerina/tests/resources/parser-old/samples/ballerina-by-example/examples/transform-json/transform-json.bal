import ballerina/http;
import ballerina/lang.messages;
import ballerina/lang.errors;
import ballerina/lang.system;
import ballerina/doc;

@doc:Description {
    value: "Defining Person struct"
}
struct Person {
    string name;
    int age;
    string city;
}

@http:configuration {
    basePath: "/person"
}
@doc:Description {
    value: "Defining Person service which provides person details"
}
service<http> PersonService {

    @http:resourceConfig {
        methods:["POST"],
        path:"/"
    }
    @doc:Description {
        value: "Defining POST resource for the service to get person details"
    }
    resource getPerson (message m) {
        // Get the json payload from the request
        json j = messages:getJsonPayload(m);
        
        // Declare a Person variable
        Person p;
        
        // Declare a type conversion error to accept any type conversion errors thrown
        errors:TypeConversionError err;
        // Convert json to a Person type variable
        p, err = <Person> j;
        
        // Print if an error is thrown
        if (err != null) {
            system:println(err);
        }
        
        // Define a constant city value as "London"
        string city = "London";
        
        // Create a new json variable constrained by Person struct
        json<Person> response = {};

        // Use p, Person variable as input to transform fields of response json which is the output
        transform {
            response.name = p.name;
            response.age = p.age;
            response.city = city;
        }
        
        // Set the new json payload to the message
        messages:setJsonPayload(m, response);
        
        // Reply from the resource with message m
        response:send(m);
    }
}
