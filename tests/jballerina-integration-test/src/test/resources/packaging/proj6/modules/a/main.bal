import ballerina/lang.'object;
import ballerina/test;

int count = 0;
function init() {
   incrementCount();
   assertCount(1);
}

public function main() {
}

public class ABC {

    *'object:Listener;
    private string name = "";

    public function init(string name){
        self.name = name;
    }

    public function __start() returns error? {
       incrementCount();
       if (self.name == "ModA") {
        assertCount(5);
       } else if (self.name == "ModB") {
        assertCount(6);
       } else if (self.name == "ModC") {
        assertCount(7);
       }
    }

    public function __gracefulStop() returns error? {
       incrementCount();
       if (self.name == "ModC") {
        assertCount(8);
        panic error("Stopped module C");
       } else if (self.name == "ModB") {
        assertCount(9);
        panic error("Stopped module B");
       } else if (self.name == "ModA") {
        assertCount(10);
        panic error("Stopped module A");
       }
    }

    public function __immediateStop() returns error? {
    }

    public function __attach(service s, string? name = ()) returns error? {
    }

    public function __detach(service s) returns error? {
    }
}

listener ABC ep = new ABC("ModA");

public function incrementCount() {
    count += 1;
}
public function assertCount(int val) {
    test:assertEquals(count, val);
}