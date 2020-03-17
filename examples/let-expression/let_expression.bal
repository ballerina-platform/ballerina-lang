import ballerina/io;

public function main() {

    // This is a simple let expression, which involves only a single
    // variable declaration.
    int a = let int b = 1 in b * 2;
    io:println("a: ", a);

    // Declare multiple variables in a let expression.
    string greeting = let string hello = "Hello ",
                          string ballerina = "Ballerina!"
                      in hello + ballerina;
    io:println("greeting: ", greeting);

    // The scope of a variable declared in a let expression is from
    // the point of declaration to the end of the let expression.
    int three = let int one = getInt(), int two = one + one in one + two;
    io:println("three: ", three);

    // Declare variables using the keyword `var` in a let expression.
    int length = let var num = 10, var txt = "four" in num + txt.length();
    io:println("length: ", length);

    [int, int] v1 = [10, 20];
    // Let variable declarations can include binding patterns.
    int tupleBindingResult = let [int, int] [d1, d2] = v1,
                                 int d3 = d1 + d2
                             in  d3 * 2;
    io:println("tuple binding result: ", tupleBindingResult);

    int age = let Person {
                    name: firstName,
                    age: personAge,
                    ...otherDetails
              } = getPerson()
              in personAge;
    io:println("age: ", age);

    boolean fatal = let var error(reason, ...params) = getSampleError()
                    in params["fatal"];
    io:println("fatal: ", fatal);
}

public function getInt() returns int => 1;

type Person record {
    string name;
    int age;
    string country;
};

function getPerson() returns Person => {
    name: "John",
    age: 31,
    country: "USA",
    "occupation": "Lawyer"
};

function getSampleError() returns error {
    return error("SampleError", message = "Detail message", fatal = true);
}
