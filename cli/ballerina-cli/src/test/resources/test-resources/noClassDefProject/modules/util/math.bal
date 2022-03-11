# Note - returns lower and (optionally) higher bound indexes.
#
# + arr - Parameter Description
# + return - Return Value Description  
public function indexOfMedian(int[] arr) returns record {|int a; int? b;|} {
    if arr.length() % 2 == 0 { // even
        return {
            a: (arr.length() / 2) - 1,
            b: arr.length() / 2
        };
    } else { // odd
        return {
            a: arr.length() / 2,
            b: ()
        };
    }
}
public function power (int x, int y) returns int {
    if y == 0 { return 1; }
    int tmp = power(x, y/2);
    if y % 2 == 0 {
        return tmp * tmp;
    } else {
        return x * tmp * tmp;
    }
}