function testFunction() {
    functionWithReturn().
    io:println("SUBSCRIBE");
}

# This is a test function
#
# + return - Return Value Description
function functionWithReturn() returns Record1 {
    return {};
}

type Record1 record {
    int testOptional?;
};
