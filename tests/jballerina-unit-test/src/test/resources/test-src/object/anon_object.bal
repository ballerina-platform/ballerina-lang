
function testAnonObjectAsFuncParam() returns int {
    return testAnonObjectFunc(10, object {
                                      public int k = 14;
                                      public string s = "sameera";
                                  });
}

function testAnonObjectFunc(int i, object {
                                       public int k;
                                       public string s;
                                   } anonSt) returns int {
    return anonSt.k + i;
}

function testAnonObjectAsLocalVar() returns (int) {
    object {
        public int k;
        public string s;
    } anonSt = object {
                   public int k = 11;
                   public string s = "";
               };

    return anonSt.k;
}

object {
    public string fname;
    public string lname;
    public int age;
} person = object {
               public string fname = "";
               public string lname = "";
               public int age = 0;

               function init() {
                   self.fname = "default fname";
                   self.lname = "default lname";
               }
           };

function testAnonObjectAsPkgVar() returns (string) {

    person = object {
                 public string fname = "";
                 public string lname = "";
                 public int age = 0;

                 function init() {
                     self.fname = "sameera";
                     self.lname = "jaya";
                 }
             };
    person.lname = person.lname + "soma";
    person.age = 100;
    return person.fname + ":" + person.lname + ":" + person.age.toString();
}

class employee {
    public string fname;
    public string lname;
    public int age;
    public object {
               public string line01;
               public string line02;
               public string city;
               public string state;
               public string zipcode;
           } address;

    public object {
               public string month;
               public string day;
               public string year;
           } dateOfBirth;

    function init(string fname, string lname, int age, object {
                                                           public string line01;
                                                           public string line02;
                                                           public string city;
                                                           public string state;
                                                           public string zipcode;
                                                       } address, object {
                                                                      public string month;
                                                                      public string day;
                                                                      public string year;
                                                                  } dateOfBirth) {
        self.fname = fname;
        self.lname = lname;
        self.age = age;
        self.address = address;
        self.dateOfBirth = dateOfBirth;
    }
}

function testAnonObjectAsObjectField() returns (string) {

    employee e = new ("sam", "json", 100, object {
                                              public string line01 = "12 Gemba St APT 134";
                                              public string line02 = "";
                                              public string city = "Los Altos";
                                              public string state = "CA";
                                              public string zipcode = "95123";
                                          }, object {
                                                 public string month = "JAN";
                                                 public string day = "01";
                                                 public string year = "1970";
                                             });
    return e.dateOfBirth.month + ":" + e.address.line01 + ":" + e.address.state + ":" + e.fname;
}

object {
    public int age;
    public string name;
    function getName() returns string;
} p = object {
          public int age;
          public string name;

          function init() {
              self.age = 5;
              self.name = "a " + "hello";
          }

          function getName() returns string {
              return self.name;
          }
      };

function testAnonObjectWithFunctionAsGlobalVar() returns string {
    return p.getName();
}

function testAnonObjectWithFunctionAsLocalVar() returns string {
    object {
        public int age;
        public string name;

        function getName() returns string;
    } p1 = object {
               public int age = 0;
               public string name = "";

               function init() {
                   self.name = "a " + "hello";
               }

               function getName() returns string {
                   return self.name;
               }
           };
    return p1.getName();
}

public class Person {
    public int age = 0;
    public string name = "";
    public int length = 0;
    public string kind = "";

    public function init(int age, string name, int length) {
        self.name = name;
        self.age = age;
        self.length = length;
    }

    public function getName() returns string {
        return self.name;
    }

    public function getKind() returns string {
        return self.kind;
    }
}

function testObjectEquivalencyBetweenAnonAndNormalObject() returns [int, string, string] {
    object {
        public int age;
        public string name;
        public int length;
        public string kind;

        public function getName() returns string;

        public function getKind() returns string;
    } value = object {
                  public int age = 0;
                  public string name = "";
                  public int length = 0;
                  public string kind = "";

                  public function init() {
                      self.name = "passed Name";
                      self.age = 5;
                      self.kind = " hello " + "sample value";
                  }
                  public function getName() returns string {
                      return self.name;
                  }

                  public function getKind() returns string {
                      return self.name + self.kind;
                  }
              };

    Person person1 = value;

    return [person1.age, person1.name, person1.getKind()];
}

function testAnonObjectWithRecordLiteral() returns [int, string] {
    object {
        public record {| int age; string name; anydata...; |} details;
        int length;
        string kind;

        function getName() returns string;
    } value = object {
                  public record {| int age; string name; anydata...; |} details;
                  int length = 0;
                  string kind;

                  function init() {
                      self.details = {age: 8, name: "sanjiva"};
                      self.kind = "passed kind";
                  }

                  function getName() returns string {
                      return self.details.name;
                  }
              };

    return [value.details.age, value.getName()];
}

class Foo {
    public record {| int age; string name; anydata...; |} details;

    private int length = 0;
    private string kind = "";

    function init(record {| int age; string name; anydata...; |} details, string kind) {
        self.details = details;
        self.kind = kind;
    }

    function getName() returns string {
        return self.details.name;
    }
}

function testObjectWithAnonRecordLiteral() returns [int, string] {
    Foo value = new ({age: 8, name: "sanjiva"}, "passed kind");

    return [value.details.age, value.getName()];
}

function testObjectWithSelfReference() returns [int, string] {
    object {
        public int age;
        public string name;

        function test(int a, string n);
    } sample = object {
                   public int age;
                   public string name;
                   function init() {
                       self.age = 88;
                       self.name = "Tyler ";
                   }

                   function test(int a, string n) {
                       self.age = self.age + a;
                       self.name = self.name + n;
                   }
               };

    sample.test(10, "Jewell");
    return [sample.age, sample.name];
}

function testCodeAnalyzerRunningOnAnonymousObjectsForDeprecatedFunctionAnnotation() {
    object {
        int b;
    } obj = object {
                int b = Test();
            };
}

@deprecated
isolated function Test() returns int {
    return 0;
}
