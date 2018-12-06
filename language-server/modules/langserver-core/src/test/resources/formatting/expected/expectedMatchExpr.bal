import ballerina/io;

function getAgeCategory(int age) returns string | error {
    if (age < 0) {
        error e = {
            message: "Invalid"
        };
        return e;
    } else if (age <= 18) {
        return "Child";
    } else {
        return "Adult";
    }
}

function main(string... args) {

    var result = getAgeCategory(25);
    if (result is error) {
        ageCategory = result.message;
    } else {
        ageCategory = result;
    }

    result = getAgeCategory(-5);
    if (result is error) {
        ageCategory = result.message;
    } else {
        ageCategory = result;
    }

    var result = getAgeCategory(25);
    if (result is error) {
        ageCategory = result.message;
    } else {
        result = getAgeCategory(-5);
        if (result is string) {
            ageCategory = result;
        } else {
            ageCategory = result.message;
        }
    }
}

