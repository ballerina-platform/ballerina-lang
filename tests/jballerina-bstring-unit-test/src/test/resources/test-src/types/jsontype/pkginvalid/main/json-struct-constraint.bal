
import structdef;

function testJsonStructConstraintInvalid () returns (json) {
    json<structdef:Person> j = {};
    j.firstname = "John Doe";
    return j.firstname;
}
