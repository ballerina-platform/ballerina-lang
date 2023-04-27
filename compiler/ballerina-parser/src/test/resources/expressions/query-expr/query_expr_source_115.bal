function testGroupBy() {
    var buyers = from var {buyer, price} in orders
        group by buyer, price
        s
}
