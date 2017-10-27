struct Person {
    string name;
    int age;
    string city;
}

struct User {
    string username;
    string location;
    int age;
}

function getFirstName (string name) (string) {
    string[] names = name.split("");
    return names[0];
}

function main (string[] args) {
    Person p = {name:"John Allen", age:30, city:"London"};
    User u = {};

    transform {
        u.username = getFirstName(p.name.toUpperCase());
        u.location = p.city.toLowerCase().toUpperCase();
        u.age = p.age;
    }

    println(u.username);
    println(u.location);
    println(u.age);
}
