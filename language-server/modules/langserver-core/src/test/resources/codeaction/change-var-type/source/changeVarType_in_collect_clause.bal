public function main() {
    var orders = [{buyer: "b1", price: 12}, {buyer: "b2", price: 13}, {buyer: "b3", price: 14}, {buyer: "b3", price: 15}];

    int buyers = from var {price, buyer} in orders 
    collect [price];
    
    int letters = from var s in  ["a", "b", "c"] collect ", ".join(s);
}
