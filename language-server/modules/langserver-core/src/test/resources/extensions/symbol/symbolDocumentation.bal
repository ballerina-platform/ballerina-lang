import ballerina/log;

# Adds two integers.

# + x - an integer
# + y - another integer

# + return - the sum of `x` and `y`
public function add(int x, int y)
                    returns int {

    return x + y;
}

public function main() returns error? {
    int result = add(2,3);
    log:printInfo(result.toString());
}
