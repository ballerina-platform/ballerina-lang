public function main() {
    int result = getInt();
    if result > 0 {
        json response = performAction(result);
    }
}

function getInt() returns int {
    return 1;
}
