import ballerina/io;

function name() {
    map<string> words = {
        a: "apple",
        b: "banana",
        c: "cherry"
    };

    // To fetch the values defined in the map, use one variable. To fetch both the key (string) and value, use two
    // variables.
    foreach (k, v in words) {
        io:println("letter: ", k, ", word: ", v);
        break;
    }
    foreach k, v in words {
        io:println("letter: ", k, ", word: ", v);
        foreach j, l in words {
            io:println("letter: ", j, ", word: ", l);
            break;
        }
        break;
    }
}
