import ballerina.io;

@Description {value:"A const declaration can appear only as a top-level construct in Ballerina."}
const float PI = 3.14159;

function main (string[] args) {
    float circumference = 10.4;
    float diameter = circumference / PI;
    io:println("Diameter of the circle: " + diameter);
}
