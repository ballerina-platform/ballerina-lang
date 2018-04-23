import ballerina/io;

// This function takes the salary and the bonus rate as input parameters and prints.
// The first parameter is mandetory. The parameter "annualIncrement" is a defaultable parameter. 
// Its an optional parameter with a default value of 20. Defaultable parameters must be always 
// defined after the required parameters.
function printSalaryDetails (int baseSalary, int annualIncrement = 20, float bonusRate = 0.02) {
    io:println("Base Salary: " + baseSalary + " | Annual Increment: " + annualIncrement + " | Bonus Rate: " + bonusRate);
}

function main (string... args) {
    // Call the function by passing only the salary. The annualIncrement and the bonusRate will be defaults to 0.
    printSalaryDetails(2500);

    // Call the function by passing only the salary and annualIncrement. The defalutable parameter needs  
    // to be paassed as key-value pairs, when invoking the function. The bonusRate will be defaults to 0.
    printSalaryDetails(2500, annualIncrement = 100);

    // Call the function by passing only the salary and bonusRate. The annualIncrement will be defaults to 0.
    printSalaryDetails(2500, bonusRate = 0.1);


    // Call the function by passing all three parameters.
    printSalaryDetails(2500, annualIncrement = 100, bonusRate = 0.1);

    // Position of the defaultable arguments can be mixed when invoking the function.
    printSalaryDetails(2500, bonusRate = 0.1, annualIncrement = 100);

    // The position of the defaultable arguments can be even mixed with the required arguments, invoking the function.
    printSalaryDetails(bonusRate = 0.1, annualIncrement = 100, 2500);
}
