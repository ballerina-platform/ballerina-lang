const constLength = 2;
int intLength = 2;
const stringLength = "2";

function arraySizeTest() {
    int[constLength] a = [1, 2, 3];
    int[intLength] b = [1, 2];
    int[stringLength] c = [1, 2];
    int[length] d = [1, 2];
    int[2] e = [1, 2, 3];

    int[2][constLength] f = [[1,2],[1,2,3]];
    int[2][intLength] g = [[1,2],[1,2]];
    int[2][stringLength] h = [[1,2],[1,2]];
    int[2][length] i = [[1,2],[1,2]];
    int[2][2] j = [[1,2],[1,2,3]];

    int[constLength][2] k = [[1,2],[1,2],[1,2]];
    int[intLength][2] l = [[1,2],[1,2]];
    int[stringLength][2] m = [[1,2],[1,2]];
    int[length][2] n = [[1,2],[1,2]];
    int[2][2] o = [[1,2],[1,2],[1,2]];

    int[2][constLength][2] p = [[[1,2],[1,2],[1,2]],[[1,2],[1,2]]];
    int[2][intLength][2] q = [[[1,2],[1,2]],[[1,2],[1,2]]];
    int[2][stringLength][2] r = [[[1,2],[1,2]],[[1,2],[1,2]]];
    int[2][length][2] s = [[[1,2],[1,2]],[[1,2],[1,2]]];
    int[2][2][2] t = [[[1,2],[1,2],[1,2]],[[1,2],[1,2]]];
}