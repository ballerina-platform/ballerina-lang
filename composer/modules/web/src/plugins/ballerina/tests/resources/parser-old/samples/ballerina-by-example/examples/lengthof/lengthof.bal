import ballerina/lang.system;

function main (string... args) {
    //Here you create integer array with with several integer elements.
    int[] intArray = [1, 2, 3];

    int arrayLength;

    //Print the created integer array length calculated by 'lengthof' unary operator.
    arrayLength = lengthof intArray;
    system:println("Integer array size : " + arrayLength);


    //Here you create json array with with several json elements.
    json jsonArray = [{"name":"John", "age":31},
                      {"name":"Neal", "age":22}];

    //Print the created json array length calculated by 'lengthof' unary operator.
    arrayLength = lengthof jsonArray;
    system:println("Json array size : " + arrayLength);
}

