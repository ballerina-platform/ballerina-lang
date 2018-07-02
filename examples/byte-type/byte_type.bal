import ballerina/io;

function main(string... args) {
    byte a = 12;
    io:println(a);

    byte[] arr1 = [5, 24, 56, 243];
    byte[] arr2 = base16 `aeeecdefabcd12345567888822`;
    byte[] arr3 = base64 `aGVsbG8gYmFsbGVyaW5hICEhIQ==`;

    io:println(lengthof arr1);
    io:println(arr1[1]);

    io:println(lengthof arr2);
    io:println(arr2[2]);

    io:println(lengthof arr3);
    io:println(arr3[3]);

    byte[][] arr4 = [[1, 2, 3], [23, 45, 117, 213], [45, 3, 254, 65, 78, 99]];
    io:println(lengthof arr4);
    io:println(lengthof arr4[2]);
}
