function testGroupBy() {
    var res = from var {price1, price2, name} in orders
        group by price1
        select name;
}
