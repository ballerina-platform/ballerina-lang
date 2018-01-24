
function main(string[] args) {
    simpleWorkers();
    println("worker run finished");
}

function simpleWorkers() {
    worker w1 {
        int p = 15;
        int q = 5;
        // Invoke Some random Logic.
        int a = calculateExp1(p , q);
        println("worker 1 - " + a);
        a -> w2;
        a <- w2;
    }
    worker w2 {
        int a = 0;
        int p = 15;
        int q = 5;
        // Invoke Some random Logic.
        int b = calculateExp3(p , q);
        println("worker 2 - " + b);
        a <- w1;
        b -> w1;
    }
}

function calculateExp1(int x, int y) (int) {
    int z;
    while(x >= y) {
        y = y + 1;
        if(y == 10){
            z = 100;
            break;
        }
        z = z + 10;
    }
    return z;
}

function calculateExp3(int x, int y) (int) {
    int z;
    while(x >= y) {
        y = y + 1;
        if(y == 10){
            z = 100;
            break;
        }
        z = z + 10;
    }
    return z;
}