struct Person {
    string name;
    int age;
    string street;
    string city;
}

struct User {
    string username;
    string location;
    int age;
}

function getFirstName (string name) (string) {
    string[] names = name.split(" ");
    return names[0];
}

function main (string... args) {
    Person p = {name:"John Allen", age:30, street: "York St", city:"London"};
    User u = {};

    u = <User> p;

    println(u.username);
    println(u.location);
    println(u.age);
}

transformer <Person p, User u> {
    u.username = getFirstName(p.name.toUpperCase());
    u.location = p.street + ", " + p.city;
    u.age = p.age;
}
