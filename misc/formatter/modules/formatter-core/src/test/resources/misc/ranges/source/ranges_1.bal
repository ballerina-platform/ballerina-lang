import  ballerina/io;
import     ballerina/lang.'int;
import     ballerina/lang.'int as    x;
import     ballerina/lang.'int    as  y;

function  testFunc()     {
        int|error     i1     =  'int:fromString("100");
      int|error     i2     =  x:fromString("100");
     int|error   i3     =  y:fromString("100");
}

class  Foo  {
    public    string  s    =    "";
}

type Bar   record {
    string     s =   "";
};

type Baz  1|2;

public     const     Baz   BAZ  = 1; //:11:1:: 'Baz'

object  {     //     No errors     due  to    anon     types.
                public  string   s     =  "";
             public  Baz     z = 1;
                 public    Foo    foo1  = new;

             }   anonObj   =  new;

public     class    ParentFoo     {

    public     int  i =     0;
     public    ChildFoo   c     =   new("");    // :23:5:: 'ChildFoo'
      public    Foo     f =    new;               // :24:5::    'Foo'
    public  Baz  z     =   1;               //  :25:5::   'Baz'


    public   function init    (int  i,  ChildFoo  c){        //     :28:5::    'ChildFoo'
        self.i     = i;
          self.c   =    c;
    }
}

class   ChildFoo  {

     private    string  name     =  "";


       function     init(string   name) {
            self.name    =   name;
      }
}
//    :44:1:: 'Bar',     :44:55::  'Foo',    :44:76::   'Foo'
public   function     fooFunc2    (string    s, Bar r,     int     i =    4,     Foo...     fArg)   returns  Foo  {
    Foo  f   =  new     ();
       return    f;
}
//    :49:1::  'Bar',   :49:55::    'Foo', :49:76:: 'Foo'
public  function fooFunc3     (string    s,    Bar  r, int   i   =     4,  Foo...   fArg)     returns    [Foo,  string]     {
        Foo  f    =   new  ();
       return [f,  "G"];
}

public     function   fooFunc1   (Foo     fArg)     { //     :54:1::  'Foo'
      Foo     fooVar    =    fArg;
}


public   function  BazFunc    (Foo...   fArg) returns   (Baz)     { // :59:26::    'Foo',    :59:48:: 'Baz'
       Baz  z =     1;
      return    z;
}
//  TODO:   Fix     me.    This    is    a bug.
public function  test1(object   {
                                              public string s  =    "";
                                             public    Baz    z     =   1;
                                            public  Foo   foo1   =     new;

                                         }    anonObj1)  returns  string    {
      return    "K";
}
// TODO:  Fix     me.     This     is  a     bug.
public function test2()     returns    object   {

                                             public    string    s    =  "";
                                              public   Baz  z     =   1;
                                                public    Foo    foo1     =     new;

                                          }{
        object  {

           public    string    s     = "";
         public  Baz    z =    1;
        public     Foo foo1   = new;

    }    m   = new;

    return m;
}
// TODO:    Fix  me.    This    is a  bug.
function   test3()     returns  string  {
     object  {

            public  string s  =  "";
            public     Baz z     =   1;
          public    Foo    foo1  =  new;

    } m =   new;

      return  m.s;
}
//   TODO:     Fix me.   This is a    bug.
function     test4() returns  string   {
    object    {

         public string s  =  "";
         public Baz z = 1;
          public   Foo    foo1   =  new;

    }   m  =     new;

    return    m.s;
}

type FooRecord  record    {
      string s =   "";
      BarRecord   br = {};
};

type  BarRecord   record    {
       string  s  =  "";
};

record  {
         string     s =  "";
        Baz     z     =  1;
     Foo  foo1 =    new;
         BarRecord br  =   {};
}    anonRecord     =   {};

public   type ParentRecord     record    {
    int i    =   0;
    ChildFoo    c   =  new(""); //   :133:5::  'ChildFoo'
       ChildRecord     r  =     {}; //  :134:5:: 'ChildRecord'
     Foo   f    =  new;      //     :135:5::     'Foo'
      Baz  z    = 1;     //    :136:5:: 'Baz'
};

type    ChildRecord    record    {
        string     name     =     "";
};

function  test5()  returns string     {
     record    {
            string    s    =    "";
           Baz z  = 1;
         Foo     foo1   =     new;        //    No   error
           BarRecord   br  =  {};         //     No   error
      }   m     =  {};

      return  m.s;
}
//     :154:1::   'Baz',    :154:1::    'Foo',    :154:1::   'BarRecord'
public  function     test6(record {
                              string     s    = "";
                             Baz   z =    1;
                          Foo   foo1  =    new;
                             BarRecord  br     = {};
                          }     anonRecord1)  returns     string     {
    return   "K";
}
// 163:33::    'Baz',  :163:33:: 'Foo',     :163:33:: 'BarRecord'
public    function test7()  returns record   {
                                       string     s    =   "";
                                    Baz     z    =    1;
                                       Foo     foo1     =   new;
                                       BarRecord    br    =   {};
                                }{
       record  {
          string   s  =  "";
            Baz   z =     1;
           Foo    foo1 =    new;
              BarRecord   br     =     {};
          } m    = {};

      return   m;
}

class FooTypeObj   {

    public string    s   = "";

}

type  BarTypeRecord    record     {
    string    s =     "";
};

type    BazTypeFinite  1|2;

public type  TypeA    FooTypeObj;

public     type TypeB     BarTypeRecord;

public   type     TypeC     BazTypeFinite;


public   class   FooTypePublicObj  {

    public  string   s     = "";

}

public type     BarTypePublicRecord     record     {
       string    s =   "";
};

public   type    BazTypePublicFinite   1|2;

public    type    TypeD  FooTypePublicObj;

public    type    TypeE     BarTypePublicRecord;

public type    TypeF  BazTypePublicFinite;

public type    Person     record {
   string   name     =  "";
};

type   TypeAliasOne  Person;

type  TypeAliasTwo TypeAliasOne;

public    type TypeAliasThree    TypeAliasTwo;


type   A   int;

type    B   A;

public     type    C  B;

function    assert(anydata  expected,  anydata    actual)   {
       if    (expected    !=   actual)     {
            typedesc<anydata>  expT   =  typeof     expected;
            typedesc<anydata> actT   = typeof  actual;
           string     detail  =   "expected   ["     +    expected.toString()   + "]     of   type  [" +     expT.toString()
                               +  "],     but     found  [" +   actual.toString() +     "]    of type    ["   +  actT.toString()     +    "]";
           panic    error("{AssertionError}",   message = detail);
      }
}

annotation  W   on   type, class;
annotation    map<int>  X on     record     field;
annotation    map<string>   Y  on    object     field;
annotation    Z    on     field;

@W
type     Foo record {
      string     s;
     @Z
        @X     {
          p:   glob
       }   int   i;
};

@W
class   Bar   {
        @Y    {
        q:     "hello",
          r:     "world"
        }
       @Z
     int j    =     1;
}

int   glob   =  2;

public     function  testStructureAnnots() returns    [typedesc<record  {}>, typedesc<object {}>]    {
     glob  =     123;

      Foo f  =    {s:     "",    i:   1};
       Bar    b  =  new;

    return [typeof  f,    typeof   b];
}

#
#     +    anUnion  -    value    of    param1
#  + anInt     -   value of    param2
# +  rest     -    value    of     rest     param
function  insert     (string  |    int     |    float  anUnion, int   anInt     = 1, string...  rest)    {

}

#
#    +    anUnion -     value of  param1
#    +    anInt  -  value   of    param2
#    +  rest     -     value     of  rest    param
function    insert     (string    |    int    |    float     anUnion, int    anInt   =  1,    string... rest)  {

}

public  enum     Color    {
    RED,
    GREEN,
      BLUE
}

public   enum   Language    {
        ENG  =     "English",
     TL     =    "Tamil",
    SI   =   "Sinhala"
}

public  enum    Bands   {
        QUEEN,
     PF   =   "Pink     "  + "Floyd"
}

string  colorOfSky    =     BLUE;
string globalLang    =    ENG;
string  rockBand =    PF;

function   testBasicEnumSupport()  {
      assert(RED,  "RED");
       assert(ENG,   "English");
        assert(PF,    "Pink    Floyd");

        string     a    =   RED;
       assert(a,   "RED");

        string    si  =     SI;
      assert(si,  "Sinhala");

        string   q  =   QUEEN;
    assert(q,    "QUEEN");
}

function   testElvisTupleTypePositive()  returns   [string,  int]  {
        [string,     int]|()  xT  =  ["Jack",   23];
        [string,  int]   dT  =    ["default",     0];
     var     rT =    xT     ?: dT;
       return   rT;
}

type Person     record  {|
        string  firstName;
      string lastName;
        int   age;
|};

function  testSimpleSelectQueryWithSimpleVariable() returns    Person[]  {

     Person   p1    =     {firstName:   "Alex",  lastName:  "George",     age:   23};
    Person    p2    =  {firstName:   "Ranjan",  lastName: "Fonseka",    age:  30};
      Person  p3   =     {firstName:  "John",    lastName:     "David",  age: 33};

     Person[] personList   =  [p1, p2,     p3];

    Person[]     outputPersonList    =
               from    var person   in   personList
              select    {
                    firstName:   person.firstName,
                     lastName:  person.lastName,
                       age:   person.age
             };

    return  outputPersonList;
}

function    testRecordsWithOptionalFields()     {
        map<string>  m   =    {"a":"aaa",   "b":"bbb"};

     record  {|
            string a?;
           string c?;
           string...;
        |}   r   =  m;

     assert("aaa",    r?.a);
        assert((),  r?.c);

        r.a   =  "AA";
       r.c    = "CC";

        assert(<record  {| string a?; string c?;     string...;  |}>{"a": "AA",   "b":    "bbb", "c":    "CC"},    r);
}

string    str  =   "";
do    {
    error err     =    error("custom error",     message     =     "error   value");
 str  +=  "Before    failure throw";
 fail     err;
     str   +=   "After   failure throw";
}
on  fail  error  e     {
  str  +=  "->   Error   caught   !  ";
}
str    +=  "->   Execution   continues...";

function    ifElseScope(int     number) returns     (int)   {
int  i     = number;
     if(i    ==     1) {
        i    =     -10;
           int    j    =    2;
          if(j   == 2)  {
              int     k   =   200;
                i    =    k;
         }  else    {
               int   k    =  -1;
        }
          }    else  if (i  ==  2)     {
            int    j  =   400;
         i    =     j;
         }  else   {
          i =     100;
         int   j   =     500;
           i     =  j;
    }
       return i;
}

function    listMatchPattern3(any    v)    returns    string     {
        match v   {
           [CONST1]     =>     {
            return     "[CONST1]";
           }
         [CONST1,   CONST2]  =>    {
                return   "[CONST1,   CONST2]";
        }
          [[CONST1,    CONST2]]    =>    {
                return  "[[CONST1,    CONST2]]";
          }
           [[CONST1], [CONST2]] =>  {
            return    "[[CONST1],    [CONST2]]";
        }
            [[CONST1],    [CONST2], [CONST1,    CONST2]]    =>  {
             return     "[[CONST1],    [CONST2],   [CONST1, CONST2]]";
        }
       }

      return    "No  match";
}

public class MyRetryManager    {
       private int  count;
      public     function   init(int    count  =   3)    {
        self.count     =     count;
       }
    public function     shouldRetry(error?   e)    returns   boolean {
            if    e    is     error    &&    self.count     >     0  {
                self.count    -=   1;
               return   true;
           } else    {
              return false;
        }
    }
}

function testRetryStatement()  {
    string|error    x =   retryError();
      if(x   is    string)   {
          assertEquality("start attempt  1:error,    attempt    2:error, attempt   3:result returned     end.",    x);
    }
}

function   retryError()     returns   string   |error     {
       string     str  =     "start";
        int   count   = 0;
     retry<MyRetryManager> (3) {
        count     =   count+1;
           if    (count    <  3)  {
               str     += ("  attempt    "   +    count.toString() +   ":error,");
            return  trxError();
           }
          str    +=  ("  attempt    "+    count.toString()  + ":result  returned    end.");
          return     str;
      }
}

function     trxError()      returns    error  {
    return    error("TransactionError");
}

type  AssertionError   error;

const    ASSERTION_ERROR_REASON =    "AssertionError";

function    assertEquality(any|error    expected,  any|error  actual)   {
       if expected  is    anydata    &&  actual    is anydata     &&     expected   ==  actual   {
          return;
       }

     if     expected ===   actual  {
            return;
     }

      panic    AssertionError(ASSERTION_ERROR_REASON,     message   = "expected    '"     +   expected.toString()  +    "',     found  '"     + actual.toString  ()     +    "'");
}

function testLocalTransaction1(int i)    returns   int|error {
     int     x     =     i;

     transaction   {
           x +=  1;
         check     commit;
       }

     transaction {
          x    +=    1;
          check     commit;
       }
       return   x;
}

public   function     func()    {
     string[] args   =   <@tainted>  ["hello",     "taint",   "analyzer"];
        secureFunction(args);
}

function     secureFunction  (@untainted   string[]   sensitiveInput) {

}

function     testLambdaFunctions()   returns    string    {
        var     fn    = function   () returns     string     {
        string  name    =  "Inside  a   lambda function";
           return   name;
        };

      return   fn();
}

public   function     workerSendToWorker()   returns   int    {
      @strand{thread:"any"}
    worker  w1    {
        int    i  =   40;
      i    ->  w2;
        }

      @strand{thread:"any"}
     worker  w2 returns    int     {
       int   j =  25;
      j   =   <-   w1;

        io:println(j);
         return     j;
      }
      int  ret     =  wait  w2;

    return     ret    + 1;
}
