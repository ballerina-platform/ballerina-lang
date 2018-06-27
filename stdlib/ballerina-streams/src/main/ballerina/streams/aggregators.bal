import ballerina/reflect;

public type Sum object {
    public {
        int sumValue = 0;
    }

    public new() {

    }

    public function process(int value, EventType eventType) returns int {
        if (eventType == "CURRENT") {
            sumValue += value;
        } else if (eventType == "EXPIRED"){
            sumValue -= value;
        } else if (eventType == "RESET"){
            sumValue = 0;
        }
        return  sumValue;
    }

    public function clone() returns Aggregator {
        Sum sumAggregator = new ();
        return sumAggregator;
    }

};

public function createSumAggregator() returns Sum {
    Sum sumAggregator = new ();
    return sumAggregator;
}




public type Aggregator object {

    public new () {

    }

    public function clone() returns Aggregator {
        Aggregator aggregator = new ();
        return aggregator;
    }

    public function process(int value, EventType eventType) returns int {
        return 10;
    }

};

