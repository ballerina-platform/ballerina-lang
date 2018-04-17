import ballerina/io;

@Description {value:"The in scope variables can be accessed by the workers in the fork-join statement."}
function main (string... args) {
    // These variables can be accessed by the forked workers.
    int i = 100;
    string s = "WSO2";
    map m = {"name":"Abhaya", "era":"Anuradhapura"};

    // Declare the fork-join statement.
    fork {
        worker W1 {
            // Change the value of the integer variable 'i' within the worker W1.
            i = 23;
            // Change the value of map variable 'm' within the worker W1.
            m["name"] = "Rajasinghe";
            // Define a new variable within the worker to send to the join block.
            string n = "Colombo";
            // Send the data to the join block of the fork-join from worker W1.
            (i, n) -> fork;
        }

        worker W2 {
            // Change the value of string variable 's' within the worker W2.
            s = "Ballerina";
            // Change the value of map variable 'm' within the worker W2.
            m["era"] = "Kandy";
            // Send the data to the join block of the fork-join from worker W2.
            s -> fork;
        }
    } join (all) (map results) {

        int p;
        string l;
        // Declare variables to receive the results from the forked workers W1 and W2.
        // The 'results' map contains a map of any type array from each worker defined within the fork-join statement
        // Values received from worker W1 are assigned to the 'any' type array of r1.
        (p, l) = check <(int, string)>results["W1"];

        // Values received from worker W2 are assigned to 'any' type array of r2.
        string q = <string>results["W2"];

        // Print the values received from workers within the join block.
        io:println("[default worker] within join:
        Value of integer from W1 is [" + p + "]");
        io:println("[default worker] within join:
        Value of string from W1 is [" + l + "]");
        io:println("[default worker] within join:
        Value of string from W2 [" + q + "]");
    }
    // Print the values after the fork-join statement to check the values of the variables.
    // The value type variables have not changed since they are passed in as a copy of the original variable.
    io:println("[default worker] after fork-join:
        Value of integer variable is [" + i + "]
        Value of string variable is [" + s + "]");
    // The reference type variables have got updated since they are passed in as a reference to the workers.
    string name;
    string era;

    var varName = <string>m["name"];
    match varName {
        string val  => {name = <string > val;}
    }

    var varEra = <string>m["era"];
    match varEra {
        string val  => {era = <string > val;}
    }

    io:println("[default worker] after fork-join:
        Value of name is [" + name + "]
        Value of era is [" + era + "]");
}
