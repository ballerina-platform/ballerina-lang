struct User {
    string name;
    int age;
}

function getUser (string name, int age) (User user) {
    user = {name:name, age:age};
    return;
}

function main (string... args) {
    User user = getUser("Ballerina", 1);
    user.<caret>
}
