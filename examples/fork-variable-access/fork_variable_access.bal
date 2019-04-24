import ballerina/io;

// The in scope variables can be accessed by the workers inside the `fork` block.
public function main() {
    // These variables can be accessed by the forked workers.
    int i = 100;
    string s = "WSO2";
    map<string> m = { "name": "Bert", "city": "New York", "postcode": "10001"};

    string name = <string> m["name"];
    string city = <string> m["city"];
    string postcode = <string> m["postcode"];

    io:println("[value type variables] before fork: " +
                   "value of integer variable is [", i, "] ",
                   "value of string variable is [", s, "]");

    io:println("[reference type variables] before fork: value " +
      "of name is [", name , "] value of city is [", city, "] value of " +
      "postcode is [", postcode, "]");

    // Declare the `fork` block.
    fork {
        worker W1 {
            // Change the value of the integer variable `i` within worker `W1`.
            i = 23;
            // Change the value of the `map` variable `m` within worker `W1`.
            m["name"] = "Moose";

            fork {
                worker W3 {
                    // Change the value of the `map` variable `m` within worker
                    // `W3`.
                    string street = "Wall Street";
                    m["street"] = street;

                    // Change the value of the integer variable `i` within
                    // worker `W3`.
                    i = i + 100;
                }
            }

            // Wait for worker `W3` to finish.
            wait W3;
        }

        worker W2 {
            // Change the value of the `string` variable `s` within worker `W2`.
            s = "Ballerina";
            // Change the value of the `map` variable `m` within the worker `W2`.
            m["city"] = "Manhattan";
        }
    }

    // Wait for both workers `W1` and `W2` to finish.
    _ = wait {W1, W2};

    // Print the values after the `fork` block to check if the values of the
    // variables have changed.

    // The value type variables have changed since the original variables are passed in
    io:println("[value type variables] after fork: " +
               "value of integer variable is [", i, "] ",
               "value of string variable is [", s, "]");

    // The internal content of the reference type variables have got updated 
    // since they are passed in as a reference to the workers.
    name = <string> m["name"];
    city = <string> m["city"];
    // Get value of the new field added to `map` variable `m` inside worker `W3`.
    string street = <string> m["street"];
    io:println("[reference type variables] after fork: " +
               "value of name is [", name,
               "] value of city is [", city, "] value of street is [", street,
               "] value of postcode is [", postcode, "]");
}
