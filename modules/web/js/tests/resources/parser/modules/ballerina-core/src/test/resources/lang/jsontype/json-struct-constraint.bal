struct Person {
    string name;
    int age;
    string address;
}

function testJsonStructConstraint() (json, json, json, string, int, string) {
    json<Person> j = {};
    j.name = "John Doe";
    j.age = 30;
    j.address = "London";
    var name, _ = (string) j.name;
    var age, _ = (int) j.age;
    var address, _ = (string) j.address;
    return j.name, j.age, j.address, name, age, address;
}

function testJsonInitializationWithStructConstraint() (json, json, json){
    json<Person> j = {name:"John Doe", age:30, address:"London"};
    return j.name, j.age, j.address;
}
