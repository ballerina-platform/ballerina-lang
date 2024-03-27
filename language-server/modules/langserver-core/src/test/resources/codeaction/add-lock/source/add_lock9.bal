isolated map<int[]> & readonly mp = {"a": [1, 2]};

isolated function fn() returns int[]? {
    return mp["a"];
}
