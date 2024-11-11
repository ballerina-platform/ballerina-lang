const TRUE = true;

function testWhileStatementWithEndlessLoop() returns int {
   int x = 1;
   while true {
      x += 1;
   }
}

function testWhileStatementWithEndlessLoop2() returns int|never {
   int x = 1;
   while TRUE {
      x += 1;
   }
}

function testWhileStatementWithEndlessLoop3() returns int {
   int x = 1;
   while true {
      while true {
         while true {
            x += 1;
         }
      }
   }
}

function testWhileStatementWithEndlessLoop4() returns int {
   int x = 0;
   while true {
     x += 1;
     if (x == 50) {
        // Intentionally no break statement
     }
   }
}

function testInfiniteLoopWhileStatementInWorker() returns future<int> {
    worker name returns int {
        while true {

        }
    }
    return name;
}

function testInfiniteLoopWhileStatementInAnonymousFunction() {
    function() returns int fn = function () returns int {
        while true {

        }
    };
    _ = fn();
}


function testInfiniteLoopWhileStatementInConditionalStatement(int x) returns int {
    if x > 10 {
        while true {

        }
    } else {
        while true {

        }
    }
}

client class MyClientClass {
    resource function accessor path() returns int {
        while true {

        }
    }

    remote function testInfiniteLoopWhileStatementInRemote() returns int {
        while true {

        }
    }
}
