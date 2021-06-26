public function getStackTrace() returns error:CallStackElement[] {
    error e = error("error!");
    return e.stackTrace().callStack;
}
