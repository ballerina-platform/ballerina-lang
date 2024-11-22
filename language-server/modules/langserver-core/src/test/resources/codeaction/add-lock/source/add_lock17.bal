isolated class Stacks {
    private int[][] list = [];

    isolated function get(int i) returns int[] {
        lock {
            return self.list[i].clone();
        }
    }

    isolated function put(int[] & readonly arr) {
        lock {
            self.list.push(arr);
        }
    }
}

isolated Stacks s2 = new; 

isolated function fn() {
    _ = s2.get(0);
}
