
import lang.jsontype.pkg.structdef;

function testJsonStructConstraint() (json, json, json) {
    json<structdef:Person> j = {};
    j.name = "John Doe";
    j.age = 30;
    j.address = "London";
    var name, _ = (string) j.name;
    var age, _ = (int) j.age;
    var address, _ = (string) j.address;
    return j.name, j.age, j.address;
}