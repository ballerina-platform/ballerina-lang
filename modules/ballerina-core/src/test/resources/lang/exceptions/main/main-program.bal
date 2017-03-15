package lang.exceptions.main;

function main (string[] args) {
    if ("test1" == args[0]) {
        int[] a = [11, 12, 13, 14];
        int value = a[10];
    } else if ("test2" == args[0]) {
        try {
            int[] a = [11, 12, 13, 14];
            int value = a[10];
        } catch (exception e) {
            throw e;
        }
    }
}
