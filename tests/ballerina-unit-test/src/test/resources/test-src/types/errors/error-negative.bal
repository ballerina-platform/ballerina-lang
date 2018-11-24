function test () returns string|error {
    error|string e = error("error1");
    return error("error2");
}
