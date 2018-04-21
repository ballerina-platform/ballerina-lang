
import structdef;

function testJsonStructConstraint () returns (json, json, json) {
    json<structdef:Person> j = {};
    j.name = "John Doe";
    j.age = 30;
    j.address = "London";
    var name = <string>j.name;
    var age  = <int>j.age;
    var address = <string>j.address;
    return (j.name, j.age, j.address);
}
