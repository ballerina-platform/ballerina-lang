function addSubPrecedence (int a, int b, int c) (int sum) {
    sum = a - b + c ;
    return sum;
}

function addSubMultPrecedence (int a, int b, int c, int d, int e, int f) (int sum) {
    sum = a * b - c + d * e - f;
    return sum;
}

function multDivisionPrecedence (int a, int b, int c, int d, int e, int f) (int sum) {
    sum = a * b / c * d * e / f;
    return sum;
}

function addMultPrecedence (int a, int b, int c, int d, int e, int f) (int sum) {
    sum = a * b * c + d * e + f;
    return sum;
}

function addDivisionPrecedence (int a, int b, int c, int d, int e, int f) (int sum) {
    sum = a / b / c + d / e + f;
    return sum;
}