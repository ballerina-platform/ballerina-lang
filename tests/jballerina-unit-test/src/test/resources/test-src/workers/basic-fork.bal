import ballerina/io;

function sendToFork(){
  fork {
      @concurrent{}
      worker w1 {
        int i = 23;
        i -> w2;
      }
      @concurrent{}
      worker w2{
        int j = <- w1;
        io:print(j + 1);
      }
  }
  wait w2;
}

function forkInWorker() {
    worker wx {
        fork {
            @concurrent{}
            worker w1 {
                int i = 86;
                i -> w2;
            }
            @concurrent{}
            worker w2 {
                int j = <- w1;
                io:print(j + 1);
            }
        }
        wait w2;
    }
    wait wx;
}
