int globalVal = 12;

type person record {
    string name = "";
    int age = globalVal;
};

function testFunction() {
    person newPerson = {};
    newPerson.name = "Bob";
}