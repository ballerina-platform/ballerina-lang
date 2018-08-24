function main(string... args) {
    int a = 10;
    int b = 20;

    try {
        a = 101;
    } catch (error err) {
        a = 500;
    } finally {
        b = a;
    }
}