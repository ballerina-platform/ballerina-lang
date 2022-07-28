function testGroupBy() {
    var res = from var {price1, price2, name} in orders
        group by var price = price1 + price2
        select name;
}

type Order record {|
    int price1;
    int price2;
    string name;
|};
