import ballerina/io;

public function main() {
    // Define a `byte` variable.
    byte byteVal = 12;
    io:println(byteVal);

    // Create a `byte` array using the list constructor.
    byte[] byteArray1 = [5, 24, 56, 243];
    // Create a `byte` array using the `base16` byte array literal.
    byte[] byteArray2 = base16 `aeeecdefabcd12345567888822`;
    // Create a `byte` array using the `base64` byte array literal.
    byte[] byteArray3 = base64 `aGVsbG8gYmFsbGVyaW5hICEhIQ==`;

    io:println(byteArray1);
    io:println(byteArray2);
    io:println(byteArray3);
}
