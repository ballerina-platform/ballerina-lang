import ballerina/io;

class StockKeeper {
    int requestCount = 0;
    int amount = 0;
    public function add(int n) {
        // Locks the `amount` field and increments it by `n`.
        lock {
            self.amount += n;
        }
        // Increments the `requentCount` field. This does not lock the field.
        self.requestCount += 1;
    }
}

StockKeeper stockKeeper = new;

public function main() {
    // Two workers executing the `add` method in `StockKeeper` concurrently
    // in which their strands are executed in their own threads.
    @strand {
        thread:"any"
    }
    worker w1 {
        foreach var i in 1 ... 1000 {
            stockKeeper.add(25);
        }
    }
    @strand {
        thread:"any"
    }
    worker w2 {
        foreach var i in 1 ... 1000 {
            stockKeeper.add(25);
        }
    }
    _ = wait { w1, w2 };
    // The amount would have a consistent result always since it was locked before incrementing.
    io:println("Amount: ", stockKeeper.amount);
    // The request count will be inconsistent between runs since its field access was not 
    // secured with locking to provide correct concurrent access behavior.
    io:println("Request Count: ", stockKeeper.requestCount);
}
