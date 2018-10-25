type User record {
    string name;
    int age;
};

function getUser(string name, int age) returns User {
    User user = { name: name, age: age };
    return;
}

public function main(string... args) {
    var user = getUser("Ballerina", 1);
    user.<caret>
}
