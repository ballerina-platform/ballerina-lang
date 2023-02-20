int a = 10;
int b = 20;

public function main() {
    worker w1 {
        lock {
            a += 1;
            3 ->> w2;
            b += 1;
        }
    }

    worker w2 {
        lock {
            b += 1;
            int x = <- w1;
            a += 1;
            x += 1;
        }
    }
}

public function test() {
    worker A {
        lock {
        int num = 10;
        num -> B;
        string msg = <- B;
        }
    }

    worker B {
        lock {
            int num;
            num = <- A;
            string msg = "Hello";
            msg -> A;
            num += 1;
        }
    }
}
