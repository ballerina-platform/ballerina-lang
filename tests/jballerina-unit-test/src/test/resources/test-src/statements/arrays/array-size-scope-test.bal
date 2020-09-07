const constLength = 2;
int intLength = 2;
const stringLength = "2";

//in module scope
//int[constLength] a = [1, 2, 3]; // clarify
int[intLength] b = [1, 2];
int[stringLength] c = [1, 2];
int[length] d = [1, 2];

//inside function scope
function arraySizeReferenceInDifferentScopeTest() {
    int[constLength] e = [1, 2, 3];
    int[intLength] f = [1, 2];
    int[stringLength] g = [1, 2];
    int[length] h = [1, 2];

    //inside anonymous function scope
    function () anonFunction =
                function () {
                   int[constLength] i = [1, 2, 3];
                   int[intLength] j = [1, 2];
                   int[stringLength] k = [1, 2];
                   int[length] l = [1, 2];
                };

    anonFunction();

    //in block scope
    int x = 10;

    if (x == 10) {
        int[constLength] m = [1, 2, 3];
        int[intLength] n = [1, 2];
        int[stringLength] o = [1, 2];
        int[length] p = [1, 2];
    }
}

