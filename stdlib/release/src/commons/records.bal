public type Module record {|
    string name;
    string 'version;
    int level;
    boolean release;
    boolean releaseInProgress = false;
|};
