public function main() returns error? {
    check testArray([1]);
}

function testArray(int[] input) returns error? {
    int[] arr = from var val in input
        select check add(val);
}

function add(int a) returns int|error {
    return error("Invalid value");
}