import ballerina/http;

public function main() {
    worker w1 {
        int i = 100;
        i -> w
    }
    
    worker w3 {
        int mw;
        mw = <- w1;
    }
}
