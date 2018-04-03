import ballerina/io;

function main (string[] args) {
    //Here you create integer array with with several integer elements.
    int[] intArray = [1, 2, 3];

    int arrayLength;

    //Print the length of the created integer array calculated by the 'lengthof' unary operator.
    arrayLength = lengthof intArray;
    io:println("Integer array size : " + arrayLength);


    //Here you create JSON array with several JSON elements.
    json jsonArray = [{"name":"John", "age":31},
                      {"name":"Neal", "age":22}];

    //Print the length of the created JSON array calculated by the 'lengthof' unary operator.
    arrayLength = lengthof jsonArray;
    io:println("JSON array size : " + arrayLength);
}
