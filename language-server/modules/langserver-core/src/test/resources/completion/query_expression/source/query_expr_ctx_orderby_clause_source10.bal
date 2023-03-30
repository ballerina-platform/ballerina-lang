function testQueryExpression() {

    int[] topX = from int i in [1, 2, 3]
        order by i d
        limit 10
        select i;
}
