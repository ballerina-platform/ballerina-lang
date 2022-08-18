import ballerina/iox;
import ballerina/math;

public function main() {
    iox:println("Hello, World!");
}

# Prints PI value
# Refers math library
public function print(int value) {
    // Refer symbols of another module.
    // `math:PI` is a qualified identifier. Note the usage of the module prefix.
    float piValue = math:PI;

    // Use the explicit prefix `console` to invoke a function defined in the `ballerina/io` module.
    iox:println(piValue);
    if value == 0 {
        iox:println("Value cannot be zero.");
    } else if value < 0 {
        iox:println("Value cannot be negative.");
    } else {
        iox:println("Value is acceptable.");
    }

    map<string> countryCapitals = {
        "USA": "Washington, D.C.",
        "Sri Lanka": "Colombo",
        "England": "London"
    };
    foreach var [country, capital] in countryCapitals.entries() {
        iox:println("Country: ", country, ", Capital: ", capital);
    }

    int[] numbers = [1, 3, 4, 7];
    while (numbers.length() > 0) {
        iox:println(numbers.pop());
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
         iox:println("Attempting execution...");
         check print();
     }

     do {
         print(1);
     } on fail Error e {
         iox:println("Error caught: ", e.message());
     }

     lock {
         print(2);
     }
}

public function pi() returns float = @java:FieldGet {
    name: "PI",
    'class: "java/lang/Math"
} external;
