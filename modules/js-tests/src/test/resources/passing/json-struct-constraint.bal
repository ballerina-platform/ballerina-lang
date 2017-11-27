package main;

import structdef;

function testJsonStructConstraintInvalid () (json) {
    json<structdef:Person> j = {};
    j.firstname = "John Doe";
    return j.firstname;
}
