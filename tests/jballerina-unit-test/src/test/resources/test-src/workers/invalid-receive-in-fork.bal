public function main() {
    fork {
        worker w1{
            int i = 20;
        }

        worker w2 {
            int sum = 0;
            fork {
                worker w3 {
                    int l = 50;
                    l -> w4;
                }
                worker w4 {
                    foreach var j in 1... 10 {
                        var i = j;
                        sum = sum + i;
                        if (true) {
                           i = <- w3;
                        }
                    }
                }
            }
        }
    }
}
