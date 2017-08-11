package lang.jsontype.pkginvalid.main;

import lang.jsontype.pkg.structdef;

function testJsonStructConstraintInvalid() (json) {
    json<structdef:Person> j = {};
    j.firstname = "John Doe";
    return j.firstname;
}