import ballerina/httpx;

public function main() {
    worker w1 {
        int i = 100;
        i -> 
    }
    
    worker w3 {
        int mw;
        mw = <- w1;
    }
}
