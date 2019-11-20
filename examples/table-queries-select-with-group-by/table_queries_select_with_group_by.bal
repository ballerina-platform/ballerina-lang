import ballerina/io;

// This `record` type represents an order.
type Order record {
    int personId;
    int orderId;
    string items;
    float amount;
};

// This `record` type represents the summed-up order details.
type SummedOrder record {
    int personId;
    float amount;
};

public function main() {
    string queryStmt = "";

    // These are a few sample values, which represent orders made by 
    // the person records listed above.
    Order o1 =
    {personId: 1, orderId: 1234, items: "pen, book, eraser", amount: 34.75};
    Order o2 =
    {personId: 1, orderId: 2314, items: "dhal, rice, carrot", amount: 14.75};
    Order o3 =
    {personId: 2, orderId: 5643, items: "Macbook Pro", amount: 2334.75};
    Order o4 = {personId: 3, orderId: 8765, items: "Tshirt", amount: 20.75};

    // This is the in-memory `table`, which is constrained by the `Order` type.
    table<Order> orderTable = table {
        {personId, orderId, items, amount},
        [o1, o2, o3, o4]
    };

    // This prints the content of the `Order` table.
    printTable(queryStmt, "The orderTable: ", orderTable);

    // Querying a `table` always returns a new in-memory `table`.
    // This applies the `group by` clause to a `table` and returns a new `table` with the result.
    table<SummedOrder> summedOrderTable = from orderTable
                  select personId, sum(amount) group by personId;
    queryStmt = "\ntable<SummedOrder> summedOrderTable = " +
            "from orderTable select personId, sum(amount) group by personId;";
    printTable(queryStmt, "summedOrderTable: ", summedOrderTable);
}

function printTable(string stmt, string tableName, table<anydata> returnedTable) {
    io:println(stmt);
    io:println(tableName);
    foreach var row in returnedTable {
       io:println(row);
    }
}
