   function name0() {

}

type Person record {
    string name;
    string location?;
};

function name1(){float c = 0;}

function name2(int a,string b){
  // Test comment.
float c = 0;}

function name3(){}

function name4(   string s,float i) {}

# test annotation
function name5() {}

     public       function    main(   string... args){}

function   name6(int a,string... i){}

function   name7()returns int{
return 0;
}

function     name8()returns [int,string]{
return [ 0,""];
}

function   name9() returns ( Person ) {
    return { name:"" };
}

         function name10() {

}

function name14(   int i,   string name="john" ,  string...  rest){

}

function name15(  int i , string name = "john",  string...  rest ) {

}

function name16(  int i, string name = "john",int id=0 ,    string ...    rest) {

}

function name17(int i,string name = "john",
                   int id = 0, string... rest) {

}

   function name18()=    @ java : Method    {name: "dfadfa",
'class: "a.b.Foo"
   }    external  ;

    function name19() =  @ java : Method  {   name :  "dfadfa",'class:"a.b.Foo"   }   external;

public function main1() {
            _=start     foo  (  )  ;
 }

function foo() returns int {
           return 1;
  }

  public function main2() {
     _
        =
         start
    foo();
  }

  function toEmployee1(Person p) returns Employee=>{
          name :  p . fname  +  " "  +  p . lname  +  " "  +  x . toString()
  };

  function toEmployee2(Person p) returns Employee
         =>
       {
  name :  p . fname  + " "  +  p.lname + " " + x.toString()
   } ;