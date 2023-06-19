function testGroupBy() {
    var res = from var {price1, price2, name} in orders
        group by var _ = true
        select name;
}
