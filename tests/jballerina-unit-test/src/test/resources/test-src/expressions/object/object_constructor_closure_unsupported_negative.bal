function test1() returns any {
    final int i = 10;           // mapBlock2

    object {    // zz = 2
            object {
                // int x;
                function bar(int b) returns int;
             } yy;

             function bar(int b) returns int;

        } zz = object {
            object { // yy = 1
                // int x;
                function bar(int b) returns int;

            } yy = object {
                int x = 5 + i;
                function bar(int b) returns int { // call me baby 1
                    return b + self.x;
                }
            };

            function bar(int b) returns int {   // call me baby 2
                return b + self.yy.bar(b);
            }
        };

        return zz.bar(4);
}

function test2() returns any {
    final int i = 10;           // mapBlock2

    object {    // zz = 2
            object {
                // int x;
                function bar(int b) returns int;
             } yy;

             function bar(int b) returns int;

        } zz = object {
            object { // yy = 1
                // int x;
                function bar(int b) returns int;

            } yy = object {
                int x = i;
                function bar(int b) returns int { // call me baby 1
                    return b + self.x;
                }
            };

            function bar(int b) returns int {   // call me baby 2
                return b + self.yy.bar(b);
            }
        };

        return zz.bar(4);
}
