package ballerina.util.arrays;

@Description {value:"Concatenate elements of an string array using a delimiter."}
@Param {value:"arr: string array to be concatenated"}
@Param {value:"delimiter: delimiter which should be used in the concatenation"}
@Return {value:"string: concatenated string"}
public function concat (string[] arr, string delimiter) (string) {
    int size = lengthof arr;
    int index = 0;
    string result = "";
    while (index < size) {
        // If the current element is the last element in the array, we don't need to append the delimiter.
        if (index == size - 1) {
            result = result + arr[index];
            break;
        } else {
            result = result + arr[index] + delimiter;
            index = index + 1;
        }
    }
    return result;
}
