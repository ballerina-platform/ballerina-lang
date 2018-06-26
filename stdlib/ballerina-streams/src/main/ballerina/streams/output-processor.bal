import ballerina/io;

public type OutputProcess object {
    private {
        function (any) outputFunc;
    }

    new(outputFunc) {

    }

    public function process(StreamEvent[] streamEvents) {
        io:println("fdfdfdfd");
        io:println("Events at output", streamEvents);
        StreamEvent[] newStreamEventArr = [];
        int index = 0;
        foreach event in streamEvents {
            io:println("YYYYYYYY");
            io:println("YYYYYYYY : ", event.eventObject);
            outputFunc(event.eventObject);
        }
    }
};

public function createOutputProcess(function (any) outputFunc) returns OutputProcess {
    OutputProcess outputProcess = new(outputFunc);
    return outputProcess;
}