function returnError1() returns error? {
    return error("Something went wrong");
}

public function main() returns error? {
    check returnError1() on fail e => 
