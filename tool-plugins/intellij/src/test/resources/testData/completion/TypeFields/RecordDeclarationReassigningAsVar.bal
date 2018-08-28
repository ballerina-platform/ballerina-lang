type User record {
    string name;
    int age;
};

public function main (string... args) {
    User user = {};
    var temp = user;
    temp.<caret>
}
