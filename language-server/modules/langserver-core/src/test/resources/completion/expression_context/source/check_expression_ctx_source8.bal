public function main() returns error? {
    string[] arr = ["bal", "ballerina"];
    stream<string, error?> myStream = arr.toStream();

    check myStream.
}
