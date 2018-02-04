import ballerina.util.arrays;

function testStringArrayConcatenation (string[] arr, string delimiter) (string) {
    return arrays:concat(arr, delimiter);
}
