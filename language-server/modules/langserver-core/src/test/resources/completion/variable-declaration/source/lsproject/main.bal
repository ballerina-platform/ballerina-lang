import lsproject.models;

public function main() {
    int num = ;
    models:Person person = 
}

function getOddNumber() returns int {
    return 5;
}

function getAge() returns (int) {
    return 10;
}

function stringFunction() returns string {
    return "";
}

function getPerson() returns models:Person {
    return {
        age: 10,
        country: "Sri Lanka",
        name: "John Doe"
    };
}

function getDriver(models:Person person) returns models:Driver {
    return {
        ...person,
        drivingLicenceNo: "12222"
    }
}