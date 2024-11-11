function testGroupBy() {
    var res = from var {price1, price2, name} in orders
        group by int price = price1 + price2
        select name;
}
