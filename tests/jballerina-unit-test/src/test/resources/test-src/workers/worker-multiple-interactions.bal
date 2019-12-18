function testMultiInteractions(int k) returns int{
    return test(k);
}

function test (int k) returns int {
    @concurrent{}
    worker mainW returns int {
        int x = 1100;
        x -> w1;
        x = <- w3;
        return x;
    }

    @concurrent{}
    worker w1 {
        int x = 0;
        x = <- mainW;
        x = x + 1;
        x -> w2;
    }

    @concurrent{}
    worker w2 {
        int x = 0;
        x = <- w1;
        x = x + 1;
        x -> w3;
    }

    @concurrent{}
    worker w3 {
        int x = 0;
        x = <- w2;
        x = x + 1;
        x -> mainW;
    }

    return wait mainW;
}
