import ballerina/runtime;
import ballerina/io;


type person object {
    string stars;

    public function update(string s) {
            lock {
                foreach i in 1 ... 1000 {
                                  self.stars = self.stars + s;
                                }
            }

        }
};

person p1 = new;

function lockFieldInSameObject() returns string {
     worker w1 {
         p1.update("*");
     }
     worker w2 {
         p1.update("#");
         runtime:sleep(10);
         return p1.stars;
     }
 }

type Student object {
    int score;
};

Student student = new;

function fieldLock() returns int {
    workerFunc();
    return student.score;
}

function workerFunc() {

    worker w1 {
        increment();
    }

    worker w2 {
        increment();
    }

}

function increment() {
    lock {
       foreach i in 1 ... 1000 {
           student.score = student.score + i;
       }
    }
}

