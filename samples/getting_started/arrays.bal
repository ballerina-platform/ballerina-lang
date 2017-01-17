import ballerina.lang.array;
import ballerina.lang.system;

function main (string[] args) {

    //Creating an array.
    int[] arr;

    //Adding values to the array.
    arr = [ 1, 2, 3];

    //Adding a new value to the array.
    arr[3] = 4;

    system:println(arr[3]);
    //prints 4.
    system:println(array:length(arr));
    //prints 4.
}