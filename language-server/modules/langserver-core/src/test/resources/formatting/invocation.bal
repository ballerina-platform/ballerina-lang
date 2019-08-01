type Person1 object {
    string name = "john";
    public function start() returns error? {
        return;
    }

    public function test1() returns error? {
        return   self . start();
    }

    public function test2() returns error? {
        return   self . test1();
    }

    public function getName() returns string {
        return   self . name;
    }
};

function invocationObj() returns string {
    Person1 p = new();
    string name =  p . getName();
    return  p .  getName();
}

type Person2 object {
    string name = "john";
    public function start() returns error? {
        return;
    }

    public function test1() returns error? {
        return         self   .
                start();
    }

    public function test2() returns error? {
        return    self  .
test1();
    }

    public function getName() returns string {
   return     self   .
     name;
    }
};

type Person3 object {
    string name = "john";
    public function start() returns error? {
        return;
    }

    public function test1() returns error? {
     return
                self
             .
                start()
           ;
    }

    public function test2() returns error? {
             return
      self
.
    test1()
                       ;
    }

    public function getName() returns string {
        return
self
.
name
;
    }
};