isolated map<int[]> mp = {"a": [1, 2]};

isolated function fn(int[] & readonly inputArr) {
    mp["a"] = inputArr;
}
