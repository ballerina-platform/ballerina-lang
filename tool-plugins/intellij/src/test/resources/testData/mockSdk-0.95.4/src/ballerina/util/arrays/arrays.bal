
@Description {value:"Copies the specified range of the specified string array "}
@Param {value:"anyArrayFrom: The any array from which the range will be copied"}
@Param {value:"anyArrayTo: The any array to which the range will be copied"}
@Param {value:"from: The initial index of the range"}
@Param {value:"to: The final index of the range"}
@Return {value:"Number of elements copied"}
public native function copyOfRange (any anyArrayFrom, any anyArrayTo, int from, int to) (int);

@Description {value:"Sorts the specified string array "}
@Param {value:"arr: The string array to be sorted"}
@Return {value:"string[]): The sorted array"}
public native function sort (string[] arr) (string[]);

@Description {value:"Copies the specified any array"}
@Param {value:"anyArrayFrom: The from array to be copied"}
@Param {value:"anyArrayTo: The to array to which to copy to"}
@Return {value:"Number of elements copied"}
public native function copyOf (any anyArrayFrom, any anyArrayTo) (int);

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
