int i = getInt();

function getInt() returns (int){
    worker w1 {
        return 1;
    }
    worker w2 {
        sleep(10000);
    }
}

function test()(int){
    return i;
}
