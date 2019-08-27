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
    // Call the function by passing a value only for the `baseSalary` parameter.
    // The `annualIncrement` and `bonusRate` parameters default to
    // 20 and 0.02 respectively.
    printSalaryDetails(2500);

    // Call the function by passing values only for the `baseSalary` and `annualIncrement`
    // parameters. The value for the `annualIncrement` parameter is passed as a named argument.
    // The `bonusRate` parameter defaults to 0.02.
    printSalaryDetails(2500, annualIncrement = 100);

    // Call the function again by passing values only for the `baseSalary` and `annualIncrement`
    // parameters, now passing the value for the `annualIncrement` parameter as a positional argument.
    // The `bonusRate` parameter defaults to 0.02.
    printSalaryDetails(2500, 100);

    // Call the function by passing values only for the `baseSalary` and `bonusRate` parameters.
    // The `annualIncrement` parameter defaults to 20.
    printSalaryDetails(2500, bonusRate = 0.1);

    // In order to pass the value for `bonusRate` as a positional argument, a value would
    // have to be specified for the `annualIncrement` parameter too.
    // All arguments are positional arguments here.
    printSalaryDetails(2500, 20, 0.1);

    // Call the function by passing values for all three parameters, the first argument as
    // a positional argument and the rest as named arguments.
    printSalaryDetails(2500, annualIncrement = 100, bonusRate = 0.1);

    // Call the function by passing all three arguments as named arguments.
    // Any and all arguments after the first named argument need to be specified
    // as named arguments but could be specified in any order.
    printSalaryDetails(annualIncrement = 100, baseSalary = 2500, bonusRate = 0.1);
}
