import ballerina.net.http;

@Description {value:"Defining Person struct."}
struct Person {
    string name;
    int age;
    string city;
}

@http:configuration {
    basePath:"/person"
}
@Description {value:"Defining Person service which provides person details."}
service<http> PersonService {

    @http:resourceConfig {
        methods:["POST"],
        path:"/"
    }
    @Description {value:"Defining POST resource for the service to get person details."}
    resource getPerson (http:Request req, http:Response res) {
        // Get the JSON payload from the request
        json j = req.getJsonPayload();

        // Declare a Person variable
        Person p;

        // Declare a type conversion error to accept any type conversion errors thrown
        TypeConversionError err;
        // Convert JSON to a Person type variable
        p, err = <Person>j;

        // Print if an error is thrown
        if (err != null) {
            println(err);
        }

        // Define a constant city value as "London".
        string city = "London";

        // Create a new json variable constrained by Person struct.
        json<Person> response = {};

        // Use p, Person variable as input to transform fields of response JSON which is the output.
        transform {
            response.name = p.name;
            response.age = p.age;
            response.city = city;
        }

        // Set the new JSON payload to the message
        res.setJsonPayload(response);

        // Reply from the resource with message m.
        res.send();
    }
}
