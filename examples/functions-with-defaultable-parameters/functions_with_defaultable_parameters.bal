import ballerina/io;

// This function takes the base salary, annual increment, and bonus rate as input
// parameters and prints them. The first parameter `baseSalary` is a required
// parameter. The next two parameters `annualIncrement` and `bonusRate` are 
// two defaultable parameters. They are optional parameters that have default
// values of 20 and 0.02 respectively. Defaultable parameters must always be
// defined after the required parameters.
function printSalaryDetails(int baseSalary,
                            int annualIncrement = 20,
                            float bonusRate = 0.02) {

    io:println("Base Salary: ", baseSalary, 
                " | Annual Increment: ", annualIncrement,
                " | Bonus Rate: ", bonusRate);
}

public function main() {
    // Calls the function by passing only the `baseSalary`.
    // The `annualIncrement` and the `bonusRate` defaults to 20 and 0.02
    // respectively.
    printSalaryDetails(2500);

    // Calls the function by passing only the `baseSalary` and `annualIncrement`.
    // Passes the defaultable parameter as a key-value pair when invoking the
    // function. The `bonusRate` defaults to 0.02.
    printSalaryDetails(2500, annualIncrement = 100);

    // Calls the function by passing only the `baseSalary` and `bonusRate`.
    // The `annualIncrement` defaults to 20.
    printSalaryDetails(2500, bonusRate = 0.1);

    // Calls the function by passing all three parameters.
    printSalaryDetails(2500, annualIncrement = 100, bonusRate = 0.1);

    // The placement of the defaultable arguments can be mixed when invoking the
    // function.
    printSalaryDetails(2500, bonusRate = 0.1, annualIncrement = 100);
}
