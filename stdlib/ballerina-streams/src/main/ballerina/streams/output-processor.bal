import ballerina/io;

public type OutputProcess object {
    private {
        function (any) outputFunc;
    }

    new(outputFunc) {

    }

    public function process(StreamEvent[] streamEvents) {
        StreamEvent[] newStreamEventArr = [];
        int index = 0;
        foreach event in streamEvents {
            outputFunc(event.eventObject);
        }
    }
};

public function createOutputProcess(function (any) outputFunc) returns OutputProcess {
    OutputProcess outputProcess = new (outputFunc);
    return outputProcess;
}