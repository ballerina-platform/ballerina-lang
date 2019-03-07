import ballerina/io;
import ballerina/runtime;
type Item record {
    string name;
    float price;
    int stockAmount;
    !...;
};

type Order record {
    string itemName;
    int orderingAmount;
    !...;
};

type OutOfStockAlert record {
    string itemName;
    int stockAmount;
    !...;
};

stream<Order> orderStream = new;

table<Item> itemStockTable = table {
    { name, price, stockAmount},
    [
        {"Book", 100.0, 10},
        {"Pen", 20.0, 4}
    ]
};

stream<OutOfStockAlert> oredrAlertStream = new;

function initOutOfStockAlert() {
    forever {
        from orderStream window length(1) as itemOrder
        join queryItemTable(itemOrder.itemName, itemOrder.orderingAmount) as item
        select item.name as itemName, item.stockAmount
        => (OutOfStockAlert[] alerts) {
            foreach var alert in alerts {
                oredrAlertStream.publish(alert);
            }
        }
    }
}

function initOutOfStockAlert2() {
    forever
    {
        from orderStream window length(1) as itemOrder
        join queryItemTable(itemOrder.itemName, itemOrder.orderingAmount) as item
        select item.name as itemName, item.stockAmount
        => (OutOfStockAlert[] alerts) {
            foreach var alert in alerts {
                oredrAlertStream.publish(alert);
            }
        }
    }
}

public function queryItemTable(string itemName, int orderingAmount)
returns table<Item> {
    table<Item> result = table {
        { name, price, stockAmount},
        []
    };
    foreach var item in itemStockTable {
        if (item.name == itemName && orderingAmount > item.stockAmount) {
            var ret = result.add(item);
        }
    }
    return result;
}
