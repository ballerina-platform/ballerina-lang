public function main() {
    string errorMsg;
    string errorCause;
    error(errorMsg, error(errorCause)) = error("msg", error("Error cause"));
}
