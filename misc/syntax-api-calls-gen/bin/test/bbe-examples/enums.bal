import ballerina/io;

// An enum is defined using a module enum declaration.
enum Color {
  RED,
  GREEN,
  BLUE
}

enum Language {
    // An enum member can explicitly specify an associated expression.
    EN = "English",
    TA = "Tamil",
    SI = "Sinhala"
}

// An enum declaration can consist of a combination of members with or without
// explicit associated values.
enum Bands {
    QUEEN,
    PF = "Pink " + "Floyd"
}

public function main() {
    // An enum member can be used in the same way as a string constant.
    string skyColor = BLUE;
    io:println("Color of the sky: ", skyColor);

    string concatenated = concat(QUEEN, PF);
    io:println("concatenated: ", concatenated);

    // An enum can also be used as a type.
    Language language = getLanguage("e");
    io:println("language: ", language);

    Language sinhala = "Sinhala";
    io:println("sinhala: ", sinhala);

    EN english = "English";
    io:println("english: ", english);
}

function concat(string str1, string str2) returns string {
    return str1 + " and " + str2;
}

function getLanguage(string symbol) returns Language {
    match symbol {
        "e" => {
            return EN;
        }
        "t" => {
            return TA;
        }
    }
    return SI;
}
