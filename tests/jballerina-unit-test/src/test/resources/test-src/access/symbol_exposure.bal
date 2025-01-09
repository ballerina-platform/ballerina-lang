public function main() {
    int[] nums = [1, 2, 3, 4];

    int[] numsTimes10 = from var i in nums
                        // The `select` clause is evaluated for each iteration.
                        select i * 10;
}