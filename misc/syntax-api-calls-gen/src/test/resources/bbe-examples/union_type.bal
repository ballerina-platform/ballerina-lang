import ballerina/io;

// This function returns either a `Person` or an `error`.
function lookupPerson(string|int id) returns Person|error {
    // If the `id` is a string, lookup the `Person` by name
    if id is string {
        if !personTab.hasKey(id) {
            return error("id '" + id + "' not found");
        }
        return personTab[id];
    } else {
        // If the `id` is an int, lookup the `Person` by index
        return personTab.toArray()[id];
    }
}

public function main() {
    // This function call takes in a `string` and returns a `Person` value.
    Person|error personOrError1 = lookupPerson("Peter");
    io:println(personOrError1);

    // This function call takes in a `string` and returns an `error` value.
    Person|error personOrError2 = lookupPerson("Jack");
    io:println(personOrError2);

    // This function call takes in an `int` and returns a `Person` value.
    Person|error personOrError3 = lookupPerson(1);
    io:println(personOrError3);
}

type Person record {
    readonly string name;
    string address;
};

type PersonTable table<Person> key(name);

PersonTable personTab = table [
    { name: "John", address: "77 Grove St. Deland, FL 32720"},
    { name: "Bella", address: "43 Kirkland Ave. North Attleboro, MA 02760"},
    { name: "Peter", address: "50 Bridgeton Lane Tuckerton, NJ 08087"} ];
