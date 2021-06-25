public function main() {
    worker w1 {
        int i = 100;
        () sendVal = i ->> 
    }
    
    worker w2 {
        int vW1 = 0;
        vW1 = <- w1;
    }
}
