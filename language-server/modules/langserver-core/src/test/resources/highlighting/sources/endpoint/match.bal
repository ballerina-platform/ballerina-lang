import ballerina/http;

public function main() {

int i = 1;

match i {
        0 => {
            http:Client a = new ("");
            if (i == 2) {
                http:Client abc = new ("");
            }
        }
        1 => {
            http:Client a = new ("");
        }
    }
}

