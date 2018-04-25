import ballerina/io;

function main(string... args) {
    //Create an integer array with several integer elements.
    int[] intArray = [1, 2, 3];

    int arrayLength;

    //Print the length of the created integer array. The length is calculated using the `lengthof` unary operator.
    arrayLength = lengthof intArray;
    io:println("Integer array size : " + arrayLength);


    //Create a JSON array with several JSON elements.
    json jsonArray = [
        {"name": "John", "age": 31},
        {"name": "Neal", "age": 22}
    ];

    //Print the length of the created JSON array. The length is calculated using the `lengthof` unary operator.
    arrayLength = lengthof jsonArray;
    io:println("JSON array size : " + arrayLength);
}
