function returnError() returns error? {
    return error("Something went wrong");
}

public function main() returns error? {
    return check returnError() o
}
