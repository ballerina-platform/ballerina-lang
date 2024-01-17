function testGroupBy() {
    var res = from var {price1, price2, name} in orders
        group by var price1, var price 2
        select name;
}
