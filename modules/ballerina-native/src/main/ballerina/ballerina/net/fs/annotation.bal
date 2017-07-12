package ballerina.net.fs;

import ballerina.doc;

annotation config attach service {
    string dirURI;
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

@doc:Description {value: "actionAfterProcess: [MOVE | DELETE | NONE]"}
@doc:Description {value: "actionAfterFailure: [MOVE | DELETE | NONE]"}
@doc:Description {value: "moveAfterProcess: A valid VFS URL"}
@doc:Description {value: "moveAfterFailure: A valid VFS URL"}
@doc:Description {value: "moveTimestampFormat: A date-time expression"}
@doc:Description {value: "createMoveDir: [true | false]"}
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