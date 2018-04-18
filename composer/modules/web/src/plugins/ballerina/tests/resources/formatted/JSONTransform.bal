import ballerina/lang.system;

struct Person {
    string firstName;
    string lastName;
    int age;
    string city;
}

struct User {
    string username;
    string location;
    int age;
}

function main (string... args) {
    json<Person> p = {firstName:"John", lastName:"Doe", age:30, city:"London"};
    json<User> u = {};

    transform {
        u.username = p.firstName;
        u.location = p.city;
        u.age = p.age;
    }

    system:println(u.username);
    system:println(u.location);
    system:println(u.age);
}
