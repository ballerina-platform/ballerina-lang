isolated class MyClass {
    private map<int>[] arr = [];

    function fn(map<int> a) {
        lock {
            self.arr[0] = a;
        }
    }
}

isolated client class MyClient {
    private map<map<string[]>> mp = {};

    remote function fn(map<string[]> a) {
        lock {
            self.mp["a"] = a;
        }
    }

    resource function accessor path(string[] a) {
        lock {
            self.mp["a"]["b"] = a;
        }
    }
}
