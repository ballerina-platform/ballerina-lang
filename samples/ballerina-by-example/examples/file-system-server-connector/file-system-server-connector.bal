import ballerina.net.fs;
import ballerina.lang.system;
import ballerina.lang.messages;
import ballerina.doc;

@doc:Description {value:"The config annotation has the following attributes:"}
@doc:Description {value:"dirURI: A valid file system URI"}
@doc:Description {value:"fileNamePattern: "}
@doc:Description {value:"pollingInterval: The time gap between two polls in milli seconds"}
@doc:Description {value:"cronExpression: A valid cron expression"}
@doc:Description {value:"ackTimeOut: Maximum time to wait until an ack is received (milli seconds)"}
@doc:Description {value:"perPollFileCount: Maximum no. of files to prcoess per poll"}
@doc:Description {value:"fileSortAttribute: [name | size | last-modified-date]"}
@doc:Description {value:"fileSortAscending: [true | false]"}
@doc:Description {value:"actionAfterProcess: [MOVE | DELETE | NONE]"}
@doc:Description {value:"actionAfterFailure: [MOVE | DELETE | NONE]"}
@doc:Description {value:"moveAfterProcess: A valid VFS URL"}
@doc:Description {value:"moveAfterFailure: A valid VFS URL"}
@doc:Description {value:"moveTimestampFormat: A date-time expression"}
@doc:Description {value:"createMoveDir: [true | false]"}
@doc:Description {value:"parallel: [true | false]"}
@doc:Description {value:"threadPoolSize: A positive integer indicating the required size of the thread pool"}

@fs:config {
    dirURI:"/home/ballerina/fs-server-connector/observed-dir",
    pollingInterval:"5000",
    fileSortAttribute:"name",
    fileSortAscending:"true",
    actionAfterProcess:"MOVE",
    moveAfterProcess:"/home/ballerina/fs-server-connector/moveDir/",
    createMoveDir:"true"
}
service<fs> fileSystem {
    @doc:Description {value:"A service bound to the file system can only contain one resource"}
    resource fileResource (message m) {
        // The message 'm' contains the URL of a file as its payload
        // This URL can be then used in conjunction with another connector
        system:println(messages:getStringPayload(m));
        reply m;
    }
}
