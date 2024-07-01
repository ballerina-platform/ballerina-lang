isolated class MyClass {
    private map<int>[] arr = [];

    function fn() returns map<int> {
        lock {
            return self.arr[0];
        }
    }
}

isolated client class MyClient {
    private map<map<string[]>> mp = {};

    remote function fn() returns map<string[]> {
        lock {
            return self.mp.get("a");
        }
    }

    resource function accessor path() returns string[] {
        lock {
            return self.mp.get("a").get("b");
        }
    }
}
