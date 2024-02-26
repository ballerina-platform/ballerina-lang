function foo() {
    var orders = [
        {orderId: 1, itemName: "A", price: 23.4, quantity: 2},
        {orderId: 1, itemName: "A", price: 20.4, quantity: 1},
        {orderId: 2, itemName: "B", price: 21.5, quantity: 3},
        {orderId: 1, itemName: "B", price: 21.5, quantity: 3}
    ];
    from var i in [1, 2, 3, 4]
    do {
        var quantities = from var {itemName, quantity} in orders
            group by itemName
            select {itemName, quantity: sum(quantity)};
    };
}
