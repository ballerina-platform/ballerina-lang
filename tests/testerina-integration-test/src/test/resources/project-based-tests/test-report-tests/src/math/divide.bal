# Divides two integers.
#
#
# + return - result after division or error if divisor is zero
public function divideInt() returns (int|error) {
    int a = 10;
    int b = 0;
    int|error result;
    if (b == 0) {
        result = error("division by zero");
    } else {
        result = a/b;
    }
    return result;
}
