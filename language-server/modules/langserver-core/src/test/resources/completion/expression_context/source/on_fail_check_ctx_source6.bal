function returnError() returns error? {
    return error("Something went wrong");
}

public function main() returns error? {
    check returnError() on fail e
}
