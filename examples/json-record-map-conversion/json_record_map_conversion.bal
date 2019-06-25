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
            lname: "Punke", age: 30
        }
    };
    // The below example shows how you can convert a record to a JSON object.
    // This conversion is unsafe because it may not be possible to convert some data types
    // that are defined in the record to JSON.
    // Similarly, you can use the `.stamp()` built-in method to manipulate the value
    // itself by attempting to change its type.
    json|error j = json.convert(theRevenant);
    if (j is json) {
        io:println(j);
        io:println(j.writer.lname);
    }

    // Similarly, you can convert a record to a map.
    map<anydata>|error movieMap = map<anydata>.convert(theRevenant);
    if (movieMap is map<anydata>) {
        Person|error writer = Person.convert(movieMap["writer"]);
        if (writer is Person) {
            io:println(writer.age);
        }
    }

    // The below example shows how you can convert a JSON object to a record.
    // This conversion is unsafe because the field names and types are unknown until they are executed during the runtime.
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
    Movie|error inception = Movie.convert(inceptionJ);
    if (inception is Movie) {
        io:println(inception);
    }
}
