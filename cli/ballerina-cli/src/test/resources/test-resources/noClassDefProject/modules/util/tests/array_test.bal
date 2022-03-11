import ballerina/test;

@test:Config {}
function test_partition_1() {
    test:assertEquals(partition([0,1,1,2,2,2,3,3,3,3]), [[0,1],[1,2],[2,3],[3,4]]);
    test:assertEquals(partition([0,0,1,1,2,2,2,3]),     [[0,2],[1,2],[2,3],[3,1]]);
}
@test:Config {}
function test_zip_1() {
    test:assertEquals(zip([0], [1,2,3]),     [[0,1],[0,2],[0,3]]);
    test:assertEquals(zip([1,2,3], [0]),     [[1,0],[2,0],[3,0]]);
    test:assertEquals(zip([1,2,3], [4,5,6]), [[1,4],[2,5],[3,6]]);
}
