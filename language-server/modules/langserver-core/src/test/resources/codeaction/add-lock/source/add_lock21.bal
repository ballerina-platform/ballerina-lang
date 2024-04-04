isolated int[] arr = [1, 2, 3];

isolated function name() {
    fork {
        worker A {
            int val = <- B;
            arr[2] = val;
        }

        worker B {
            arr[0] ->> A;
        }
    }
}
