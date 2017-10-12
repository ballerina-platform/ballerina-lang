import ballerina.lang.system;

function main (string[] args) {
    //Here you create integer array with with several integer elements.
    int[] intArray = [1, 2, 3];

    int arrayLength;

    //Print the length of the created integer array calculated by the 'lengthof' unary operator.
    arrayLength = lengthof intArray;
    system:println("Integer array size : " + arrayLength);


    //Here you create JSON array with several JSON elements.
    json jsonArray = [{"name":"John", "age":31},
                      {"name":"Neal", "age":22}];

    //Print the length of the created JSON array calculated by the 'lengthof' unary operator.
    arrayLength = lengthof jsonArray;
    system:println("JSON array size : " + arrayLength);
}

