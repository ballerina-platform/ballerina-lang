function queryTest() {
    int[] nums = [1, 2, 3, 4];
    int[] numsTimes10 = from var i in nums
                        select i * 10;
    int[] evenNums = from int i in nums
                     where i % 2 == 0
                     select i;
    int[] numsReversed = from int i in nums
                         order by i descending
                         select i;
}
