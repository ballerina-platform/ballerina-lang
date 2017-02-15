import ballerina.lang.system;

function main (string[] args) {
    string pathValue = system:getenv("PATH");
    system:println("Environment variable PATH: " + pathValue);
}