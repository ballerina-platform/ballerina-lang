function incorrectMapAccessTest() returns (string) {
    map animals;
    animals["dog"] = "Jimmy";
    return animals[0];
}

function accessAllFields() {
    map fruits = {"name":"John", "address":"unkown"};
    any a = fruits.*;
}