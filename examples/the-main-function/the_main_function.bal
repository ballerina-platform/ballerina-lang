import ballerina/io;

// The `main` function that accepts student information and prints out a formatted string.
// The first parameter `name` is a required parameter, while the second parameter `age` is a defaultable
// parameter with the default value `18`. The third parameter `year` is also a defaultable parameter.
// The rest parameter `modules` represents the additional arguments.
// The `main` function may return an `error` or `()`.
public function main(string name,
                     int age = 18,
                     string year = "Freshman",
                     string... modules)
                  returns error? {

    // Return an error if the name is invalid.
    if (name.length() < 6) {
        error e = error("InvalidName", message = "invalid length");
        return e;
    }

    string info = string `Name: ${name}, Age: ${age}, Year: ${year}`;

    if (modules.length() > 0) {
        info += ", Module(s): " + modules.toString();
    }
    io:println(info);
}
