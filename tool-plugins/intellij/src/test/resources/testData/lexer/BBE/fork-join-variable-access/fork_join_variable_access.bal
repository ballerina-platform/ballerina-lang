import ballerina/io;

// The in scope variables can be accessed by the workers in the fork-join statement.
function main(string... args) {
    // These variables can be accessed by the forked workers.
    int i = 100;
    string s = "WSO2";
    map m = { "name": "Abhaya", "era": "Anuradhapura" };

    string name = <string>m["name"];
    string era = <string>m["era"];
    io:println("[default worker] before fork-join: value of name is [", 
                name, "] value of era is [", era, "]");

    // Declare the fork-join statement.
    fork {
        worker W1 {
            // Change the value of the integer variable `i` within the worker W1.
            i = 23;
            // Change the value of map variable `m` within the worker W1.
            m["name"] = "Rajasinghe";
            // Define a new variable within the worker to send to the `join` block.
            string n = "Colombo";
            // Send the data to the `join` block of the fork-join from worker W1.
            (i, n) -> fork;
        }

        worker W2 {
            // Change the value of string variable `s` within the worker W2.
            s = "Ballerina";
            // Change the value of map variable `m` within the worker W2.
            m["era"] = "Kandy";
            // Send the data to the `join` block of the fork-join from worker W2.
            s -> fork;
        }
    } join (all) (map results) {
        int p;
        string l;
        // Declare variables to receive the results from the forked workers W1 and W2.
        // The `results` map contains a map of `any` type values from each worker defined within the fork-join statement.
        // The tuple value received from worker W1 is de-structured and assigned to variables `p` and `l`.
        (p, l) = check <(int, string)>results["W1"];

        // The string value received from worker W2 is assigned to the variable `q`.
        string q = <string>results["W2"];

        // Print the values received from workers within the `join` block.
        io:println("[default worker] within join: " + 
                    "value of integer variable from W1 is [", p, "]");
        io:println("[default worker] within join: " +
                    "value of string variable from W1 is [", l, "]");
        io:println("[default worker] within join: " +
                    "value of string variable from W2 is [", q, "]");
    }
    // Print the values after the fork-join statement to check the values of the variables.
    // The value type variables have not changed since they are passed in as a copy of the original variable.
    io:println("[default worker] after fork-join: " + 
               "value of integer variable is [", i, "] ",
               "value of string variable is [", s, "]");
    // The reference type variables' internal content has got updated since they are passed in
    // as a reference to the workers.
    name = <string>m["name"];
    era = <string>m["era"];

    io:println("[default worker] after fork-join: " + 
               "value of name is [", name,
               "] value of era is [", era, "]");
}
