import ballerina/jballerina.java;

function sendToFork(){
  fork {
      @strand{thread:"any"}
      worker w1 {
        int i = 23;
        i -> w2;
      }
      @strand{thread:"any"}
      worker w2{
        int j = <- w1;
        print(j + 1);
      }
  }
  wait w2;
}

function forkInWorker() {
    worker wx {
        fork {
            @strand{thread:"any"}
            worker w1 {
                int i = 86;
                i -> w2;
            }
            @strand{thread:"any"}
            worker w2 {
                int j = <- w1;
                print(j + 1);
            }
        }
        wait w2;
    }
    wait wx;
}

public function print(any|error... values) = @java:Method {
    'class: "org.ballerinalang.test.utils.interop.Utils"
} external;
