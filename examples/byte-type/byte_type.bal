import ballerina/io;

public function main() {
    // Define a `byte` variable.
    byte a = 12;
    io:println(a);

    // Define a `byte` array using the ordinary array literal.
    byte[] arr1 = [5, 24, 56, 243];
    // Define a `byte` array using the `base16` byte array literal.
    byte[] arr2 = base16 `aeeecdefabcd12345567888822`;
    // Define a `byte` array using the `base64` byte array literal.
    byte[] arr3 = base64 `aGVsbG8gYmFsbGVyaW5hICEhIQ==`;

    io:println(arr1.length());
    io:println(arr1[1]);

    io:println(arr2.length());
    io:println(arr2[2]);

    io:println(arr3.length());
    io:println(arr3[3]);

    byte[][] arr4 = [[1, 2, 3], [23, 45, 117, 213], [45, 3, 254, 65, 78, 99]];
    io:println(arr4.length());
    io:println(arr4[2].length());
}
