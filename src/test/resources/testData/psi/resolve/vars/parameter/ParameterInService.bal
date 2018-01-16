service<http> test {

    resource test (message m, int /*def*/a) {
        /*ref*/a = 10;
    }
}
