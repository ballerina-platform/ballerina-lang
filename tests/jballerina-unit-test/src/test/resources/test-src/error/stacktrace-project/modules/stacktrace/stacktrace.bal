public function getStackTrace() returns any {
    error e = error("error!");
    return e.stackTrace().callStack;
}
