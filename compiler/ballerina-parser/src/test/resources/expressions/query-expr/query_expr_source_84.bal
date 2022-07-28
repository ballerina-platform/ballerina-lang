function testGroupBy() {
    var res = from var {price1, price2, name} in orders
        group by var totPrice = price1 + price2, int multiplyPrice = price1 * 2
        select name;
}
