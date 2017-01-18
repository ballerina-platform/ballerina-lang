import ballerina.lang.system;

function main (string[] args) {
    int marks;
    string grade;

    marks = 76;

    if (marks > 75) {
        grade = "good";
    } else if (marks > 50){
        grade = "satisfactory";
    } else {
        grade = "poor";
    }

    system:println(grade);
    //prints "good".
}