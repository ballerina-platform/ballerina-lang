import ballerina/runtime;


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

function objectParamLock() returns int {
        Student stParam = new;
        workerFuncParam(stParam);
        return stParam.score;
}

function workerFuncParam(Student param) {

    worker w1 {
        incrementParam(param);
    }

    worker w2 {
        incrementParam(param);
    }

}

function incrementParam(Student param) {
   lock {
        Student inLockObj = new;
        inLockObj.score = 10;
       foreach i in 1 ... 1000 {
           param.score = param.score + i;
       }
    }
}

