public function main() {
    worker w1 {
        int i = 100;
        () sendVal = i ->> w2;
    }
    
    worker w2 {
        int vW1 = 0;
        vW1 = <- 
    }
}
