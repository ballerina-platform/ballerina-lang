
function jsonReturnFunction() returns (json) {
    json val = {PropertyName:"Value"};
    return val;
}

function getPlainJson() returns (json) {
    json j = {firstName:"John Doe", age:30, address:"London"};
    return j;
}

function getPerson() returns (json<Person>) {
    json<Person> j = {name:"John Doe", age:30, address:"London"};
    return j;
}

function getStudent() returns (json<Student>) {
    json<Student> j = {name:"John Doe", age:30, address:"Colombo", class:"5"};
    return j;
}
