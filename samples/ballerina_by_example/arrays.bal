import ballerina.lang.array;
import ballerina.lang.system;

function main (string[] args) {

    //Creating an array.
    int[] arr;

    //Initializing an array with values.
    arr = [ 1, 2, 3];

    //Adding values to the array.
    arr[3] = 4;

    //Accessing values in array
    system:println(arr[3]);
    //prints 4.
    system:println(array:length(arr));
    //prints 4.
}