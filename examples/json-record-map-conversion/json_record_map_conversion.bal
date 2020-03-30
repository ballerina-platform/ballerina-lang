import ballerina/io;

// This is a Ballerina record definition.
type Person record {
    string fname;
    string lname;
    int age;
};

type Movie record {
    string title;
    string year;
    string released;
    Person writer;
};

public function main() {
    Movie theRevenant = {
        title: "The Revenant",
        year: "2015",
        released: "08 Jan 2016",
        writer: {
            fname: "Michael",
            lname: "Punke",
            age: 30
        }
    };
    // This example shows how you can convert a record to a JSON object.
    // This conversion could return an error because it may not be possible
    // to convert some data types that are defined in the record to JSON.
    json|error j = json.constructFrom(theRevenant);
    if (j is json) {
        io:println(j.toJsonString());
        io:println(j.writer.lname);
    }

    // Similarly, you can convert a record to a map.
    map<anydata>|error movieMap = map<anydata>.constructFrom(theRevenant);
    if (movieMap is map<anydata>) {
        Person|error writer = Person.constructFrom(movieMap["writer"]);
        if (writer is Person) {
            io:println(writer.age);
        }
    }

    // This example shows how you can convert a JSON object to a record.
    // This conversion could return an error because the field names and 
    // types are unknown until they are evaluated at runtime.
    json inceptionJ = {
        title: "Inception",
        year: "2010",
        released: "16 Jul 2010",
        writer: {
            fname: "Christopher",
            lname: "Nolan",
            age: 30
        }
    };
    Movie|error inception = Movie.constructFrom(inceptionJ);
    if (inception is Movie) {
        io:println(inception);
    }
}
