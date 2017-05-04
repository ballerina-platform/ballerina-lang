function testIfStmt(int a, int b, int c) (int, int) {

    int x;
    x = 10;

    if ( a == b) {
        a = 100;

    } else if (a == b + 1){
        a = 200;

    } else  if (a == b + 2){
        a = 300;

    }  else {
        a = 400;
    }

    b = c;

    return a + x, b + 1;
}

function testAgeGroup(int age) (string) {
    string group;
    if (age > 18) {
        group = "elder";
    } else {
        group = "minor";
    }
    return group;
}

function ifElseScope(int number)(int) {
int i = number;
    if(i == 1) {
        i = -10;
        int j = 2;
        if(j == 2) {
            int k = 200;
            i = k;
        } else {
            int k = -1;
        }
      } else if (i == 2) {
         int j = 400;
         i = j;
       } else {
         i = 100;
         int j = 500;
         i = j;
    }
    return i;
}

function nestedIfElseScope(int number1, int number2)(int) {
    int i = number1;
    if(i == 1) {
        int j = number2;
        if(j == 1) {
            int k = 100;
            i = k;
        } else {
            int k = 200;
            i = k;
        }
    } else if (i == 2) {
        int j = number2;
        i = j;
        if(j == 2) {
            int k = 300;
            i = k;
        } else {
            int k = 400;
            i =k;
        }
    } else {
        i = 100;
        int j = number2;
        if(j == 3) {
            int k = 500;
            i = k;
        } else {
            int k = 600;
        i = k;
        }
    }
    return i;
}