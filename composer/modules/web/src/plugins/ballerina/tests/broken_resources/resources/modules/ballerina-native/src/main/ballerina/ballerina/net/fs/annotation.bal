
import ballerina/doc;

@doc:Description {value: "dirURI: A valid file system URI"}
@doc:Description {value: "fileNamePattern: "}
@doc:Description {value: "pollingInterval: The time gap between two polls in milli seconds"}
@doc:Description {value: "cronExpression: A valid cron expression"}
@doc:Description {value: "ackTimeOut: Maximum time to wait until an ack is received (milli seconds)"}
@doc:Description {value: "perPollFileCount: Maximum no. of files to prcoess per poll"}
@doc:Description {value: "fileSortAttribute: [name | size | last-modified-date]"}
@doc:Description {value: "fileSortAscending: [true | false]"}
@doc:Description {value: "actionAfterProcess: [MOVE | DELETE | NONE]"}
@doc:Description {value: "actionAfterFailure: [MOVE | DELETE | NONE]"}
@doc:Description {value: "moveAfterProcess: A valid VFS URL"}
@doc:Description {value: "moveAfterFailure: A valid VFS URL"}
@doc:Description {value: "moveTimestampFormat: A date-time expression"}
@doc:Description {value: "createMoveDir: [true | false]"}
@doc:Description {value: "parallel: [true | false]"}
@doc:Description {value: "threadPoolSize: A positive integer indicating the required size of the thread pool"}
annotation configuration attach service<> {
    string dirURI;
    string fileNamePattern;
    string pollingInterval;
    string cronExpression;
    string ackTimeOut;
    string perPollFileCount;
    string fileSortAttribute;
    string fileSortAscending;
    string actionAfterProcess;
    string actionAfterFailure;
    string moveAfterProcess;
    string moveAfterFailure;
    string moveTimestampFormat;
    string createMoveDir;
    string parallel;
    string threadPoolSize;
}