function incorrectArrayAccessTest() returns (string) {
    string[] animals;
    animals = ["Dog", "Cat"];
    return animals["cat"];
}

type IntOrString 1|"two";

function testIncorrectArrayAccessByFiniteType() {
    string[] animals = ["Dog", "Cat"];
    IntOrString x = 1;
    string s =  animals[x]; // should fail since x could be a string
}
