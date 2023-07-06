function testMapNegative() returns (string){
    map<any> animals;
    animals = {"animal1":"Lion", "animal2":"Cat", "animal3":"Leopard", "animal4":"Dog"};
    string value = animals["animal1"];
    return value;
}
