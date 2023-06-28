function testGroupBy() {
    var res = from var {price1, price2, name} in orders
        group by var totPrice = price1 + price2, price1
        select name;
}
