struct User {
    string name;
    int age;
}

function main (string... args) {
    User user = {};
    var temp = user;
    var temp2 = temp;
    temp2.<caret>
}
