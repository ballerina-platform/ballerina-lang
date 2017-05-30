const int /*def*/a;

service test {

    resource test (message m) {
        int value = /*ref*/a;
    }
}
