function testGroupByExpressionAndSelectWithNonGroupingKeys1() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}, {name: "Saman", price: 12}];
    int[] sum = from var {name, price} in input
                        group by name
                        select sum(price);
    assertEquality([35, 11], sum);
    sum = from var {name, price} in input
                        group by name
                        select int:sum(price);
    assertEquality([35, 11], sum);
    sum = from var {name, price} in input
                        group by name
                        select int:sum(price + 2);
    assertEquality([35, 11], sum);
    sum = from var {name, price} in input
                        group by name
                        select sum(...[price]);
    assertEquality([35, 11], sum);
    sum = from var {name, price} in input
                        group by name
                        select int:sum(2, ...[price]);
    assertEquality([35, 11], sum);
    sum = from var {name, price} in input
                        group by name
                        select 2.sum(2, ...[price]);
    assertEquality([35, 11], sum);
}


function testGroupByExpressionAndSelectWithNonGroupingKeys2() {
    var input = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Kamal", price1: 10, price2: 9},
                    {name: "Amal", price1: 10, price2: 13}];

    int[] sum = from var {name, price1, price2} in input
                    group by name
                    select sum(price1 + price2);
    // int[] sum = from var {name, price1, price2} in input
    //                 group by name
    //                 select sum(...[price1] + ...[price2]); // negative test case
    assertEquality([35, 11], sum);
    sum = from var {_, price1, price2} in input
            group by var _ = true
            select sum(price1 + price2);
    assertEquality([35, 11], sum);
    sum = from var {_, price1, price2} in input
            group by var _ = true
            select sum(price1 + price2 + 3);
    assertEquality([35, 11], sum);
    sum = from var {_, price1, price2} in input
            group by var _ = true
            select int:sum(price1 + price2 + 3);
    assertEquality([35, 11], sum);
    sum = from var {_, price1, price2} in input
            group by var _ = true
            select 5.sum(price1 + price2 + 3);
    // sum = from var {_, price1, price2} in input
    //         group by var _ = true
    //         select int:sum(price1, price2); // error
    assertEquality([35, 11], sum);
    // sum = from var {_, price1, price2} in input
    //         group by var _ = true
    //         select 5.sum(...[price1], ...[price2]); // error
    sum = from var {_, price1, price2} in input
            group by var _ = true
            select 5.sum(23, ...[price2]);
    assertEquality([35, 11], sum);
}

function testGroupByExpressionAndSelectWithNonGroupingKeys3() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}, {name: "Saman", price: 12}];
    int[] sum = from var {name, price} in input
                        group by name
                        select avg(price);
    assertEquality([35, 11], sum);
    sum = from var {name, price} in input
                        group by name
                        select int:avg(price);
    assertEquality([35, 11], sum);
    sum = from var {name, price} in input
                        group by name
                        select int:avg(price + 2);
    assertEquality([35, 11], sum);
    sum = from var {name, price} in input
                        group by name
                        select avg(...[price]);
    assertEquality([35, 11], sum);
    sum = from var {name, price} in input
                        group by name
                        select int:avg(2, ...[price]);
    assertEquality([35, 11], sum);
    sum = from var {name, price} in input
                        group by name
                        select 2.0.avg(2, ...[price]);
    assertEquality([35, 11], sum);
}

function testGroupByExpressionAndSelectWithNonGroupingKeys4() {
    var input = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Kamal", price1: 10, price2: 9},
                    {name: "Amal", price1: 10, price2: 13}];

    int[] sum = from var {name, price1, price2} in input
                    group by name
                    select avg(price1 + price2);
    // int[] sum = from var {name, price1, price2} in input
    //                 group by name
    //                 select sum(...[price1] + ...[price2]); // negative test case
    assertEquality([35, 11], sum);
    sum = from var {_, price1, price2} in input
            group by var _ = true
            select avg(price1 + price2);
    assertEquality([35, 11], sum);
    sum = from var {_, price1, price2} in input
            group by var _ = true
            select avg(price1 + price2 + 3);
    assertEquality([35, 11], sum);
    sum = from var {_, price1, price2} in input
            group by var _ = true
            select int:avg(price1 + price2 + 3);
    assertEquality([35, 11], sum);
    sum = from var {_, price1, price2} in input
            group by var _ = true
            select 5.avg(price1 + price2 + 3);
    // sum = from var {_, price1, price2} in input
    //         group by var _ = true
    //         select int:sum(price1, price2); // error
    assertEquality([35, 11], sum);
    // sum = from var {_, price1, price2} in input
    //         group by var _ = true
    //         select 5.sum(...[price1], ...[price2]); // error
    sum = from var {_, price1, price2} in input
            group by var _ = true
            select 5.avg(23, ...[price2]);
    assertEquality([35, 11], sum);
}

function testGroupByExpressionAndSelectWithNonGroupingKeys5() {
    record {|string name; float price;|}[] input1 = [{name: "Saman", price: 1.0}, {name: "Kamal", price: 1.2}, {name: "Kamal", price: 0.9}];
    float[] sum = from var {name, price} in input1
                    group by name
                    select avg(price);
    assertEquality([35, 11], sum);

    record {|string name; float price?;|}[] input2 = [{name: "Saman"}, {name: "Kamal"}, {name: "Kamal"}];
    sum = from var {name, price} in input2
                group by name
                select avg(price);
    assertEquality([35, 11], sum);

    record {|string name; float price?;|}[] input3 = [{name: "Saman"}, {name: "Kamal"}, {name: "Kamal", price: 0.9}];
    sum = from var {name, price} in input3
                group by name
                select avg(price);
    assertEquality([35, 11], sum);
}

function testGroupByExpressionAndSelectWithNonGroupingKeys6() {
    var input = [{name: "Saman", price: 11}, {name: "Saman", price: 12}, {name: "Kamal", price: 11}, {name: "Saman", price: 12}];
    int[] sum = from var {name, price} in input
                        group by name
                        select count(price);
    assertEquality([35, 11], sum);
    sum = from var {name, price} in input
                        group by name
                        select int:count(price);
    assertEquality([35, 11], sum);
    sum = from var {name, price} in input
                        group by name
                        select int:count(price + 2);
    assertEquality([35, 11], sum);
    sum = from var {name, price} in input
                        group by name
                        select count(...[price]);
    assertEquality([35, 11], sum);
    sum = from var {name, price} in input
                        group by name
                        select int:count(2, ...[price]);
    assertEquality([35, 11], sum);
    sum = from var {name, price} in input
                        group by name
                        select 2.0.count(2, ...[price]);
    assertEquality([35, 11], sum);
}

function testGroupByExpressionAndSelectWithNonGroupingKeys7() {
    var input = [{name: "Saman", price1: 11, price2: 11},
                    {name: "Saman", price1: 11, price2: 12},
                    {name: "Kamal", price1: 10, price2: 13},
                    {name: "Kamal", price1: 10, price2: 12},
                    {name: "Kamal", price1: 10, price2: 9},
                    {name: "Amal", price1: 10, price2: 13}];

    int[] sum = from var {name, price1, price2} in input
                    group by name
                    select first(price1 + price2);
    assertEquality([35, 11], sum);
    sum = from var {_, price1, price2} in input
            group by var _ = true
            select last(price1 + price2);
    assertEquality([35, 11], sum);
    sum = from var {_, price1, price2} in input
            group by var _ = true
            select first(price1 + price2 + 3);
    assertEquality([35, 11], sum);
    sum = from var {_, price1, price2} in input
            group by var _ = true
            select int:first(price1 + price2 + 3);
    assertEquality([35, 11], sum);
    sum = from var {_, price1, price2} in input
            group by var _ = true
            select 5.last(price1 + price2 + 3);
    assertEquality([35, 11], sum);
}

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }
    panic error("expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
