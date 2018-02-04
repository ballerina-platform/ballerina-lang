function incorrectMapAccessTest() (string) {
    map animals;
    animals["dog"] = "Jimmy";
    return animals[0];
}
