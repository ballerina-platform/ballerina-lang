import ballerina.lang.system;

function main (string[] args) {
    string pathValue = system:getEnv("PATH");
    system:println("Environment variable PATH: " + pathValue);
}