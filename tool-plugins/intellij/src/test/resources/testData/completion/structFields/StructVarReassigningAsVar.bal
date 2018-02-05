struct User {
    string name;
    int age;
}

function getUser (string name, int age) (User user) {
    user = {name:name, age:age};
    return;
}

function main (string[] args) {
    var user = getUser("Ballerina", 1);
    var temp = user;
    temp.<caret>
}
