public function main() {
    string|error err = withErr();
    hasInt(err);
}

function withErr() returns string|error {
    return "";
}

function hasInt(int a) {

}
