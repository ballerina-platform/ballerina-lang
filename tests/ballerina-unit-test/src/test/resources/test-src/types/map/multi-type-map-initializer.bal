function testMapInitWithDifferentTypes() returns (map<any>) {
    map<any> animals;
    animals = {"animal1":"Lion", "animal2":"Cat", "animal3":"Leopard", "animal4": 10};
    return animals;
}