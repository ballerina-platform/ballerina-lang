public function main() {
    fork {
        worker w1{
            int i = 20;
        }

        worker w2 {
            int j = 25;
            int sum = 0;
            fork {
                worker w3 {
                    foreach var i in 1... 10 {
                        sum = sum + i;
                        if (true) {
                            var xx = i ->> w4;
                        }
                    }
                }
                worker w4 {
                    int k = <- w3;
                }
            }
        }
    }
}
