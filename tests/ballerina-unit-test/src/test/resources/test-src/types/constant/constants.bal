const  name = "Ballerina";

const age = 10;

public const id = 123;

function getName() returns string {
    return name;
}

function getAge() returns int {
    return age;
}

function getId() returns int {
    return id;
}

function concatConstants() returns string {
    return "Hello " + name;
}

const GET = "GET";
const POST = "POST";

type HttpRequest "GET"|"POST";

function typeTest() returns int {
    HttpRequest req = "POST";
    int value = validate(req);
    return value;
}

function validate(HttpRequest req) returns int {
    if (req == GET){
        return 1;
    } else if (req == POST) {
        return 2;
    }
    return 0;
}
