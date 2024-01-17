function testQueryExpression() {

    int[] topX = from int i in [1, 2, 3]
        order by i  
        limit 10 d
        select i;
}
