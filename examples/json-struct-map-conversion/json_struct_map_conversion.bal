import ballerina/io;

// This is a Ballerina struct.
type person record {
    string fname;
    string lname;
    int age;
};

type movie record {
    string title;
    string year;
    string released;
    person writer;
};
// This function creates a movie object.
function main(string... args) {
    movie theRevenant = {
        title: "The Revenant",
        year: "2015",
        released: "08 Jan 2016",
        writer: {
            fname: "Michael",
            lname: "Punke", age: 30
        }
    };
    // Here's how you can convert a struct to a JSON object.
    // This conversion is unsafe because it may not be possible to convert some data types
    // that are defined in the struct to JSON.
    json j = check <json>theRevenant;
    io:println(j);
    io:println(j.writer.lname);

    // Similarly, you can convert a struct to a map. 
    // This conversion is safe.
    map m = <map>theRevenant;
    person writer = check <person>m["writer"];
    io:println(writer.age);

    // Here's how you can convert a JSON object to a struct.
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
    movie inception = check <movie>inceptionJ;
    io:println(inceptionJ);
}
