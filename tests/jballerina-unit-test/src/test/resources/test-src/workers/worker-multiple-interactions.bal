function testMultiInteractions(int k) returns int{
    return test(k);
}

function test (int k) returns int {
    worker mainW returns int {
        int x = 1100;
        x -> w1;
        x = <- w3;
        return x;
    }

    worker w1 {
        int x = 0;
        x = <- mainW;
        x = x + 1;
        x -> w2;
    }

    worker w2 {
        int x = 0;
        x = <- w1;
        x = x + 1;
        x -> w3;
    }

    worker w3 {
        int x = 0;
        x = <- w2;
        x = x + 1;
        x -> mainW;
    }

    return wait mainW;
}
