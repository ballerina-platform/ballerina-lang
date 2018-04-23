import ballerina/io;

// This function takes salary, anual increment and the bonus rate as input parameters and prints.
// The first parameter "baseSalary" is a required parameter. The next two parameters "annualIncrement" 
// and "bonusRate" are two defaultable parameters. They are optional parameters with a default value 
// of 20 and 0.02 respectively. Defaultable parameters must be always defined after the required parameters.
function printSalaryDetails (int baseSalary, int annualIncrement = 20, float bonusRate = 0.02) {
    io:println("Base Salary: " + baseSalary + " | Annual Increment: " + annualIncrement + " | Bonus Rate: " + bonusRate);
}

function main (string... args) {
    // Call the function by passing only the salary. The annualIncrement and the bonusRate will be defaults 
    // to 20 and 0.02 respectively.
    printSalaryDetails(2500);

    // Call the function by passing only the salary and annualIncrement. The defalutable parameter needs  
    // to be paassed as a key-value pair, when invoking the function. The bonusRate will be defaults to 0.02.
    printSalaryDetails(2500, annualIncrement = 100);

    // Call the function by passing only the salary and bonusRate. The annualIncrement will be defaults to 20.
    printSalaryDetails(2500, bonusRate = 0.1);

    // Call the function by passing all three parameters.
    printSalaryDetails(2500, annualIncrement = 100, bonusRate = 0.1);

    // Position of the defaultable arguments can be mixed when invoking the function.
    printSalaryDetails(2500, bonusRate = 0.1, annualIncrement = 100);

    // The position of the defaultable arguments can be even mixed with the required arguments, invoking the function.
    printSalaryDetails(bonusRate = 0.1, annualIncrement = 100, 2500);
}
