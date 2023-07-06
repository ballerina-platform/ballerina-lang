import ballerina/http;

public function main() {
    int i = 3;

    if (i < 3) {
        http:Client a = new ("");
    } else if (i < 5) {
        http:Client b = new ("");
    } else {
        http:Client c = new (""); http:Client d = new ("");
    }
}
