function testIterableOperation() {
     var orders = [{buyer: "b1", product:"beer", price: 12}, {buyer: "b2", product:"coke", price: 13}, {buyer: "b3", product:"sprite", price: 14}, {buyer: "b3", product:"fanta", price: 15}];
     
    string[] buyers = from var {buyer, product, price} in orders 
                group by buyer 
}
