import ballerina/io;
import ballerina/math;

public function main() {
    io:println("Hello, World!");
}

# Prints PI value
# Refers math library
public function print(int value) {
    // Refer symbols of another module.
    // `math:PI` is a qualified identifier. Note the usage of the module prefix.
    float piValue = math:PI;

    // Use the explicit prefix `console` to invoke a function defined in the `ballerina/io` module.
    io:println(piValue);
    if value == 0 {
        io:println("Value cannot be zero.");
    } else if value < 0 {
        io:println("Value cannot be negative.");
    } else {
        io:println("Value is acceptable.");
    }

    map<string> countryCapitals = {
        "USA": "Washington, D.C.",
        "Sri Lanka": "Colombo",
        "England": "London"
    };
    foreach var [country, capital] in countryCapitals.entries() {
        io:println("Country: ", country, ", Capital: ", capital);
    }

    int[] numbers = [1, 3, 4, 7];
    while (numbers.length() > 0) {
        io:println(numbers.pop());
    }
}

enum Color {
    RED,
    GREEN,
    BLUE
}

function miscellaneous() {
    transaction {
        check print(12);
        var res = commit;
    } on fail error er {
        io:println("Error caught during printing: ", er);
        rollback;
        fail invalidAccoundIdError;
    }

    match continent {
        "North America" => {
            return "USA";
        }
        "Antarctica" => {
            return ();
        }
    }

     fork {
        worker w1 returns int {
            string response = <string>checkpanic httpClient->get("/v4/?expr=2*3", targetType = string);
            return checkpanic int:fromString(response);
        }
     }

     retry(3) {
         io:println("Attempting execution...");
         check print();
     }

     do {
         print(1);
     } on fail Error e {
         io:println("Error caught: ", e.message());
     }

     lock {
         print(2);
     }
}

public function pi() returns float = @java:FieldGet {
    name: "PI",
    'class: "java/lang/Math"
} external;
