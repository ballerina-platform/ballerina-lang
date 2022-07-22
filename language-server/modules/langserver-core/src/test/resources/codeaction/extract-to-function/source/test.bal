const POP = 10;
int globalVar = 10;

function testFunction(int aa) {
    int population = 1000;
    int d = 0;
    int j = 0;
    int v = 9;
    v = v + 1;
    int age = 1;
    age = 2 + aa + population + globalVar;
    age = POP + age + 1 + v;
    int c = 10;
    doNothing(age + c);
}

function doNothing(int a) {

}
