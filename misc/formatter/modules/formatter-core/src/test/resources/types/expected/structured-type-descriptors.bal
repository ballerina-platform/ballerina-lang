import ballerina/io;

public function main() {
    // Arrays Structured Type Descriptor
    int[5] array1 = [1, 2, 3, 4, 5];
    int[*] array2 = [1, 2, 3, 4];
    int[] array3 = [9];
    int[][] array4 = [[1, 2, 3], [10, 20, 30], [5, 6, 7]];
    int[] array5 = [];

    // Tuple Structured Type Descriptor
    [int, string] tuple1 = [10, "John"];
    [int, string] tuple2 = [10, "John"];
    int aint;
    string astr;
    [aint, astr] = tuple2;
    [int, string] tuple3 = [10, "John"];
    var [aint1, astr1] = tuple3;
    [int, string] tuple3 = [10, "John"];
    var [q1, _] = tuple1;
}
