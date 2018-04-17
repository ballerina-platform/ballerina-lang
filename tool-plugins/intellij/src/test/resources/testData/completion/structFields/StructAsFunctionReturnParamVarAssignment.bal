struct User {
    string name;
    int age;
}

function getUser (string name, int age) (User) {
    User user = {name:name, age:age};
    return user;
}

function main (string... args) {
    var user = getUser("Ballerina", 1);
    user.<caret>
}
