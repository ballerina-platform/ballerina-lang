service<http> test {

    int /*def*/a;

    resource test (message m) {
        /*ref*/a = 10;
    }
}
