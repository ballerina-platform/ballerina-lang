import ballerina/io;

@Description {value:"The in scope variables can be accessed by the workers of in the fork-join statement."}
function main (string[] args) {
    // These variables can be accessed by the forked workers.
    int i = 100;
    string s = "WSO2";
    map m = {"name":"Abhaya", "era":"Anuradhapura"};

    // Declare the fork-join statement.
    fork {
        worker W1 {
            // Change the value of the Integer variable 'i' within the worker W1.
            i = 23;
            // Change the value of map variable 'm' within the worker W1.
            m["name"] = "Rajasinghe";
            // Define a new variable within the worker to send to the join block.
            string n = "Colombo";
            // Send the data to the join block of the fork-join from worker W1.
            i, n -> fork;
        }

        worker W2 {
            // Change the value of String variable 's' within the worker W2.
            s = "Ballerina";
            // Change the value of map variable 'm' within the worker W2.
            m["era"] = "Kandy";
            // Send the data to the join block of the fork-join from worker W2.
            s -> fork;
        }
    } join (all) (map results) {

        any[] r1;
        any[] r2;
        // Declare variables to receive the results from the forked workers W1 and W2.
        // The 'results' map contains a map of the results from the workers within the fork-join statement in the
        'any' type array.
        // Values received from worker W1 are assigned to the 'any' type array of r1.
        var x1 = <any[]>results["W1"];
        match x1 {
            any[] val  => {r1 = <any[]> val;}
            error e => {io:println(e.message);}
        }

        // Values received from worker W2 are assigned to 'any' type array of r2.
        var x2 = <any[]>results["W2"];
        match x2 {
            any[] val  => {r2 = <any[]> val;}
            error e => {io:println(e.message);}
        }

        // Get the 0th index of the array returned from worker W1.
        int p;
        p = check <int>r1[0];
        // Getting the 1st index of the array returned from worker W1.
        string l;
        var indexL = <string>r1[1];
        match indexL {
            string val  => {l = <string > val;}
        }

        // Getting the 0th index of the array returned from worker W2.
        string q;
        var indexQ = <string>r2[0];
        match indexQ {
            string val  => {q = <string > val;}
        }

        // Print the values received from workers within the join block.
        io:println("[default worker] within join:
        Value of integer from W1 is [" + p + "]");
        io:println("[default worker] within join:
        Value of string from W1 is [" + l + "]");
        io:println("[default worker] within join:
        Value of string from W2 [" + q + "]");
    }
    // Print the values after the fork-join statement to check the values of the variables.
    io:println("[default worker] after fork-join:
        Value of integer variable is [" + i + "]
        Value of string variable is [" + s + "]");

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
