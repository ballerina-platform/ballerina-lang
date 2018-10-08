const string name = "Ballerina";

const int age = 10;

function getName() returns string {
    return name;
}

function getAge() returns int {
    return age;
}

function concatConstants() returns string {
    const string hello = "Hello";
    const string ballerina = "Ballerina";
    return hello + " " + ballerina;
}
