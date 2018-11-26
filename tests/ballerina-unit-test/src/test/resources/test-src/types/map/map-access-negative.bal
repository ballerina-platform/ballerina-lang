function incorrectMapAccessTest() returns (string?) {
    map<any> animals = {};
    animals["dog"] = "Jimmy";
    return animals[0];
}

function accessAllFields() {
    map<any> fruits = {"name":"John", "address":"unkown"};
    any a = fruits.*;
}