import ballerina/runtime;
import ballerina/io;

type Item record {
    string name;
    float price;
    int stockAmount;
};

type Order record {
    string itemName;
    int orderingAmount;
};

type OutOfStockAlert record {
    string itemName;
    int stockAmount;
};

// This is the input stream that use `Order` as the constraint type.
stream<Order> orderStream = new;

// This is the table which holds the Item stock data
table<Item> itemStockTable = table {
    { name, price, stockAmount },
    [
        {"Book", 100.0, 10},
        {"Pen", 20.0, 4}
    ]
};

// This is the output stream that contains the events/alerts that are generated based on streaming logic.
stream<OutOfStockAlert> oredrAlertStream = new;

function initOutOfStockAlert() {
    //whenever an order event is published to `orderStream` it is matched against the `itemStockTable` through
    //the `queryItemTable` function. If there is a match then an alert event is published to `oredrAlertStream`.
    forever {
        from orderStream window lengthWindow(1) as o
        join queryItemTable(o.itemName, o.orderingAmount) as i
        select i.name as itemName, i.stockAmount
        => (OutOfStockAlert[] alerts) {
            foreach var alert in alerts {
                oredrAlertStream.publish(alert);
            }
        }
    }
}

//`queryItemTable` function returns a table of items whose stock is not enough to satisfy the order.
public function queryItemTable(string itemName, int orderingAmount)
        returns table<Item> {
    table<Item> result = table {
        { name, price, stockAmount }, []
    };
    foreach var item in itemStockTable {
        if (item.name == itemName && orderingAmount > item.stockAmount) {
            var ret = result.add(item);
        }
    }
    return result;
}

public function main() {
    initOutOfStockAlert();

    Order order1 = { itemName: "Pen", orderingAmount: 5};
    Order order2 = { itemName: "Book", orderingAmount: 2};

    // Whenever the `oredrAlertStream` stream receives an event from the streaming rules defined in the `forever`
    // block, the `printOutOfStocksAlert` function is invoked.
    oredrAlertStream.subscribe(printOutOfStocksAlert);

    orderStream.publish(order1);
    runtime:sleep(500);
    orderStream.publish(order2);
    runtime:sleep(500);

}

function printOutOfStocksAlert(OutOfStockAlert a) {
    io:println("Alert! : " + a.itemName +
            " stock is not enough to satisfy the order.");
}

