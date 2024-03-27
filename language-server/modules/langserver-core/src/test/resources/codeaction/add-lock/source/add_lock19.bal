isolated int[] arr = [1, 2, 3, 4, 5];

isolated client class MyClient {
    private int[] arr = [1, 2];

    isolated resource function accessor path(int i) {
        lock {
	        self.arr.push(i);
        }
    }
}

isolated function fn() {
    MyClient cl = new;
    cl->/path.accessor(arr[0]);
}
