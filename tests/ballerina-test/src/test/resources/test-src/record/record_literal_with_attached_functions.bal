
type Person {
    int age = 9;
    string name = "default first name";
};

function Person::getName() returns string {
    return self.name;
}
