import ballerina/io;

// This is a Ballerina record definition.
type Person record {
    string fname,
    string lname,
    int age,
};

type Movie record {
    string title,
    string year,
    string released,
    Person writer,
};

function main(string... args) {
    Movie theRevenant = {
        title: "The Revenant",
        year: "2015",
        released: "08 Jan 2016",
        writer: {
            fname: "Michael",
            lname: "Punke", age: 30
        }
    };
    // Here's how you can convert a record to a JSON object.
    // This conversion is unsafe because it may not be possible to convert some data types
    // that are defined in the record to JSON.
    json j = check <json>theRevenant;
    io:println(j);
    io:println(j.writer.lname);

    // Similarly, you can convert a record to a map.
    // This conversion is safe.
    map m = <map>theRevenant;
    Person writer = check <Person>m["writer"];
    io:println(writer.age);

    // Here's how you can convert a JSON object to a record.
    // This conversion is unsafe because the field names and types are unknown until runtime.
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
    Movie inception = check <Movie>inceptionJ;
    io:println(inceptionJ);
}
