import ballerina/io;
function testFailExp()  {

 //private test testFailExp() -> () {
 //       %0(RETURN) ();
 //       %0(RETURN) ();
 //       %1(LOCAL) error;
 //       %3(TEMP) string;
 //       %4(TEMP) error;
 //       %5(TEMP) ();
 //       %6(TEMP) map<anydata | readonly>;
 //       %7(TEMP) typeDesc<any | error>;
 //       %8(TEMP) map<anydata | readonly>;
 //       %9(TEMP) string;
 //       %10(TEMP) string;
 //       %11(TEMP) anydata;
 //       %12(TEMP) any | error{map<anydata | readonly>}[];
 //       %13(TEMP) int;
 //       %14(TEMP) any | error;
 //       %15(TEMP) any | error;
 //       %16(TEMP) string;
 //       %17(TEMP) ();
 //       %18(LOCAL) function() -> any | error{map<anydata | readonly>};
 //       %21(TEMP) any | error;
 //       %22(TEMP) ();
 //       %23(TEMP) any | error{map<anydata | readonly>}[];
 //       %24(TEMP) int;
 //       %25(TEMP) any | error;
 //       %26(TEMP) any | error;
 //       %27(TEMP) string;
 //       %28(TEMP) ();
 //       %29(TEMP) any | error[];
 //       %30(TEMP) int;
 //       %31(TEMP) any | error;
 //       %32(TEMP) any | error;
 //       %33(TEMP) string;
 //       %34(TEMP) ();
 //
 //       bb0 {
 //           GOTO bb1;
 //       }
 //       bb1 {
 //           %3 = ConstLoad custom error;
 //           %5 = ConstLoad 0;
 //           %4 = <error{map<anydata | readonly>}> %5;
 //   ;
 //           %9 = ConstLoad message;
 //           %10 = ConstLoad error value;
 //           %8 = NewMap %7;
 //           %11 = cloneReadOnly(%8) -> bb2;
 //       }
 //       bb2 {
 //           %6 = <map<anydata | readonly>> %11;
 //           %1 = error error(%3, %4, %6);
 //           %13 = ConstLoad -1;
 //           %16 = ConstLoad Before fail;
 //           %15 = <any | error{map<anydata | readonly>}> %16;
 //           %14 = <any | error> %15;
 //           %12 = newArray any | error[][%13];
 //           %17 = println(%12) -> bb3;
 //       }
 //       bb3 {
 //           GOTO bb4;
 //       }
 //       bb4 {
 //           %18 = fp $anon/.:0.0.0::$onFailFunc$0;
 //           %21 = FPCall %18() -> bb5;
 //       }
 //       bb5 {
 //           %22 = ConstLoad 0;
 //           GOTO bb8;
 //       }
 //       bb6 {
 //           %24 = ConstLoad -1;
 //           %27 = ConstLoad After fail;
 //           %26 = <any | error> %27;
 //           %25 = <any | error> %26;
 //           %23 = newArray any | error[][%24];
 //           %28 = println(%23) -> bb7;
 //       }
 //       bb7 {
 //           GOTO bb8;
 //       }
 //       bb8 {
 //           %30 = ConstLoad -1;
 //           %33 = ConstLoad After do !!!;
 //           %32 = <any | error> %33;
 //           %31 = <any | error> %32;
 //           %29 = newArray any | error[][%30];
 //           %34 = println(%29) -> bb9;
 //       }
 //       bb9 {
 //           %0 = ConstLoad 0;
 //           GOTO bb11;
 //       }
 //       bb10 {
 //           GOTO bb11;
 //       }
 //       bb11 {
 //           return;
 //       }
 //   }

   do {
        error err = error("custom error", message = "error value");
        io:println("Before fail");
        fail err;
        io:println("After fail");
   }
   on fail error e {
      io:println("whoops!!!!");
   }
   io:println("After do !!!");

}


function testCheckExp() returns error? {
   do {
        error err = error("custom error", message = "error value");
        io:println("Before fail");
        int res = check foo(true);
        io:println("After fail");
   }
   on fail error e {
      io:println("whoops!!!!");
   }
   io:println("After do !!!");
}

function foo(boolean er) returns int|error {
    if(er) {
        error err = error("custom error");
        return err;
    }
    return 1;
}