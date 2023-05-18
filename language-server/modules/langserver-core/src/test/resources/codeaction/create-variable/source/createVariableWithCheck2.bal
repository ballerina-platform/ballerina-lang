public function main() {
    getData();
}

type MyError error;

function getData() returns json|MyError {
    return {};
}
