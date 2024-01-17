import ballerina/module1;

type Response record {
    json body;
    module1:TestRecord1 rec1;
};

public function main() {
    int val = getInt();
}

function getInt(*Response response) returns int {
    return 1;
}