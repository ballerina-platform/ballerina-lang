import ballerina.net.http;
import ballerina.lang.messages;
import ballerina.lang.strings;
import ballerina.doc;

@doc:Description {
    value: "Defining people array"
}
Person[] people = [{name: "Ann Frank", age: 30, city: "London"},
                   {name: "Tim Yank", age: 20, city: "New York"},
                   {name: "John Grisham", age: 25, city: "Brisbane"},
                   {name: "Sarah Paulin", age: 10, city: "Chicago"},
                   {name: "Trevor Noah", age: 45, city: "Cape Town"}];


@doc:Description {
    value: "Defining Person struct"
}
struct Person {
    string name;
    int age;
    string city;
}

struct Employee {
    string first_name;
    string last_name;
    int age;
    string address;
}

@http:configuration {
    basePath: "/people"
}
@doc:Description {
    value: "Define people management service which can be accessed with /people"
}
service<http> PeopleManagementService {

    @http:resourceConfig {
        methods:["GET"],
        path:"/employee/"
    }
    @doc:Description {
        value: "Resource to get employee for the person by index which" +
        " can be invoked with /people/employee?index={index}"
    }
    resource getEmployee (message m, @http:QueryParam {value:"index"}
                                     int index) {
        //Define a struct constrained by Employee struct. With this constraint, the json, employeeJson can only
        // contain the fields of Employee struct
        json<Employee> employeeJson = {};

        // Transform statement to construct the employeeJson json with Person with the given index from the people array
        transform {
            // split name from " " to separate first and last names
            string[] _temp = strings:split(people[index].name, " ");
            employeeJson.first_name = _temp[0];
            employeeJson.last_name = _temp[1];
            employeeJson.age = people[index].age;
            employeeJson.address = people[index].city;
            // Accessing an invalid field that is not a field in Employee struct will give an error.
            // E.g. : employeeJson.city will give an error as "unknown field 'city' in json with struct constraint 'Employee'"
        }

        // Set the new employeeJson as the payload
        messages:setJsonPayload(m, employeeJson);
        reply m;
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/update"
    }
    @doc:Description {
        value: "Resource to update person by index which can be invoked with " +
        "/people/update?index={index}"
    }
    resource updatePerson (message m, @http:QueryParam{value:"index"}
                                      int index) {
        //Get the json payload from the request message m
        json j = messages:getJsonPayload(m);
        var jsonPerson, _ = (json<Person>)j;

        // Define an error that can be used during conversion from json to person struct.

        transform {
            people[index].name, _ = (string) jsonPerson.name;
            people[index].age, _ = (int) jsonPerson.age;
            people[index].city, _ = (string) jsonPerson.city;
            // Accessing an invalid field that is not a field in Employee struct will give an error.
            // E.g. : employeeJson.address will give an error as "unknown field 'address' in json with struct constraint 'Person'"
        }

        //empty the message
        m = {};

        // set the status code to 202 Accepted
        http:setStatusCode(m, 202);
        reply m;
    }


    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    @doc:Description {
        value: "Get all people which can be invoked as /people/"
    }
    resource GetPeople (message m) {
        // define empty json array
        json jsonResponse = [];
        int index = 0;
        while (index < lengthof people) {
            // convert each person from people array to a json and set to the array
            jsonResponse[index], _ = <json>people[index];
            index = index + 1;
        }

        // set the people array as the json response in the message
        messages:setJsonPayload(m, jsonResponse);
        reply m;
    }
}
