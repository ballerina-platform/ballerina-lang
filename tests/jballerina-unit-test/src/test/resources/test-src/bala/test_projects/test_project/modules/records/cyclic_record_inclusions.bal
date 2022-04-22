public type CodedMessage record {
    string code?;
    string message?;
};

public type JobRequest record {
    string processId?;
    LimitedCodedMessage[] reasons?;
    boolean success?;
};

public type JobScheduler record {
    string gridId?;
    *JobRequest;
    CodedMessage[] reasons?;
};

public type LimitedCodedMessage record {
    int|string code?;
    string message?;
};
