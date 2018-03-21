function incorrectArrayAccessTest() returns (string) {
    string[] animals;
    animals = ["Dog", "Cat"];
    return animals["cat"];
}
