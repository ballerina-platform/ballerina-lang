import ballerina.lang.system;
import ballerina.lang.jsons;

function main (string[] args) {
    // Create a JSON with a string
    json j1 = "Apple";

    // Create a JSON with an integer
    json j2 = 4;

    // Create a JSON with a float
    json j3 = 5.36;

    // Create a JSON with a boolean
    json j4 = true;

    // Create a JSON object. Keys can be defined with or without quotes. Both methods treat the keys as string. Values can be any expression or variable.
    json j5 = {name:"apple", color:"red", price:j3};
    json j6 = {"name":"apple", "color":"red", "price":j3};
    system:println(j5);
    system:println(j6);

    // Access elements in a JSON object
    system:println(j5.name);
    system:println(j5["name"]);

    // Add or update elements in aJSON object
    j5.color = "green";
    j5["price"] = 8.00; 
    system:println(j5);

    // Create a JSON array 
    json j7 = [true, "apple", j2, j6, 5.36];
    system:println(j7);

    // Access elements in a JSON array
    system:println(j7[4]);

    // Add or update elements in a JSON array
    j7[4] = 8.00; 
    system:println(j7);

    string s = jsons:toString();
    system:println(j7);
}
