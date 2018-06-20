public type Sum object {
    private {
        function (StreamEvent) nextProcessorPointer;
        string field;
    }

    new(nextProcessorPointer, field) {

    }

    public function process(StreamEvent[] streamEvents) {

    }
};

public function sum(function(StreamEvent) nextProcPointer, string field) returns Sum {
    Sum sum1 = new(nextProcPointer, field);
    return sum1;
}