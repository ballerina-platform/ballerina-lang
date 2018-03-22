function incorrectMapAccessTest() returns (string) {
    map animals;
    animals["dog"] = "Jimmy";
    return animals[0];
}
