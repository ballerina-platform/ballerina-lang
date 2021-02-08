import ballerina/bar;

# Prints `Bar`.

public function main() {
    string s = print();
}

public function print() returns string {
    return bar:printBar();
}
