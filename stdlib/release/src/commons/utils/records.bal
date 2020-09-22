public type Module record {|
    string name;
    string 'version;
    string branch;
    int level;
    boolean release;
    boolean releaseStarted = false;
|};
