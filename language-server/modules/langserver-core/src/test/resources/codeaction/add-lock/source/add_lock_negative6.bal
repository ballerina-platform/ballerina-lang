isolated int[] arr = [1, 2, 3];

isolated function name() {
    fork {
        worker A {
            arr[2] = <- B;
        }

        worker B {
            arr[0] -> A;
        }
    }
}
