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

function main (string[] args) {
    json<Person> p = {firstName:"John", lastName:"Doe", age:30, city:"London"};
    json<User> u = {};

    u = <json<User>> p;

    println(u.username);
    println(u.location);
    println(u.age);
}

transformer <json<Person> p, json<User> u> {
    u.username = p.firstName;
    u.location = p.city;
    u.age = p.age;
}
