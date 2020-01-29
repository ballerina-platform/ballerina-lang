import ballerina/http;

public function main() {

    int[2] intArray = [0, 1];

    foreach var counter in intArray {
        http:Client aa = new ("");
    }

}

