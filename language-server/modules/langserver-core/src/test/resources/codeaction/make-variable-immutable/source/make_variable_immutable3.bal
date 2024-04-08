isolated class IsolatedClass {
    record {|
        string id;
        int count;
    |} & readonly rec = {count: 0, id: ""};
}
