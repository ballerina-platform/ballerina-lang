public function main() {
    worker w1 {
        int i = 100;
        i -> w3;
        flush 
    }
    
    worker w3 {
        int mw;
        mw = <- w1;
    }
}
