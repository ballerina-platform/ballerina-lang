map<string> & readonly mp = {};

isolated function length(boolean flag) returns int {
    if flag {
        return mp.length();
    }
    return 0;
}
