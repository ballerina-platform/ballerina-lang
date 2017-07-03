package ballerina.net.smb;

annotation config attach service {
    string dirPath;
    string fileNamePattern;
    string pollingInterval;
    string cronExpression;
    string ackTimeOut;
    string perPollFileCount;
}

annotation Sort attach service {
    string fileSortAttribute;
    string fileSortAscending;
}

annotation PostProcess attach service {
    string actionAfterProcess;
    string actionAfterFailure;
    string moveAfterProcess;
    string moveAfterFailure;
    string moveTimestampFormat;
    string createMoveDir;
}

annotation Concurrency attach service {
    string parallel;
    string threadPoolSize;
}