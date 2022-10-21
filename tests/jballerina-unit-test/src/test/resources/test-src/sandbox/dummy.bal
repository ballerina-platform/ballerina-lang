
public const float NaN = 0.0/0.0;

# IEEE positive infinity.
public const float Infinity = 1.0/0.0;


public type Cloneable readonly|xml|Cloneable[]|map<Cloneable>|table<map<Cloneable>>;

const record{} REC_OPEN = {"aa": 1 + 2, "bb": "12"};
const int[] ARR_OPEN = [1,2];


// Ex. 5
const int[2] ARR_1 = [3, 1]; // list constructor - array, output = [3,1] & readonly
const int[10] ARR_2 = [0x2, 1, ...ARR_1, 1, 1]; // Fill members, output = [2,1,3,1,1,1,0,0,0,0] & readonly

// Ex. 6
const int[2]|[string, int] TUPLE_1 = ["ABC", 1]; // list constructor - tuple, output = ["ABC",1] & readonly
const [string, int, string, int, int, float] TUPLE_2 = ["ABC", 1, ...TUPLE_1, 1, 1]; // output - ["ABC",1,"ABC",1,1,1.0f] & readonly
const TUPLE_3 = ["ABC", 1, ...TUPLE_2, 1, 1]; // output - ["ABC",1,"ABC",1,"ABC",1,1,1.0f,1,1] & readonly

// Ex. 7
// Array & Tuple Type Invalid
//const string[2] ERR_ARR1 = [3, 1]; // incompatible types
//const int[1] ERR_ARR2 = [3, 1]; // incompatible types, size mismatch
//const [int, float] ERR_TUPLE2 = [3, 1, 3]; // incompatible types, size mismatch
//const [1|2] ERR_TUPLE3 = []; // no fill members
//const int[2]|[int, int] ERR_TUPLE4 = []; // ambiguous types

// Ex. 8
const map<int|string> MAP_1 = {"aa": 1 + 2, "bb": "12"}; // mapping constructor expr, output - record {| 3 aa; "12" bb; |} & readonly
const REC_1 = {"aa": 1 + 2, "bb": "12"}; // output - record {| 3 aa; "12" bb; |} & readonly
const REC_1 = {"aa": 1 + 2, "bb": "12"}; // output - record {| 3 aa; "12" bb; |} & readonly
const ABC = "abc";
const record{|int aa; string...;|} REC_2 = {"aa": 1 + 2, ABC}; // output - (record {| 3 aa; "abc" ABC; |} & readonly)
const REC_3 = {"cc": 1 + 2, ...REC_1}; // output - (record {| 3 cc; 3 aa; "12" bb; |} & readonly)

// Ex. 9
// Maps and records Invalid
//const map<int|string> ERR_MAP1 = {"aa": 1 + 2.0, "bb": "12"};
//const map<int>|record{int...} ERR_MAP2 = {"aa": 1};
//const map<int|string> ERR_MAP1 = {"aa": 1 + 2.0, "bb": "12"};

//int DEF = 123;
//const record{|int aa; (string|int)...;|} ERR_REC1 = {"aa": 1 + 2, DEF}; //
//const record{} ERR_REC2 = {"aa": 1 + 2, ...REC}; //














// // Ex. 1
// const int A = 10; // output = 10
// const int B = 1+ A + C; // binary expr, constant reference expr, output = 35
// const C = 25; // output = 25

// // Ex. 2
// const float E = (+1) + (-2); // unary expr, output = -1.0f

// // Ex. 3
// const float|decimal F = 1 + 2 - 0x1. + 7f; // output = 9.0f
// const 500|500.0 G = 100 + 400; // output = 500

// // Ex. 4
// const ERR_1 = 1 + 2 - 1 + 7.0; // + is not defined int and float
// const int ERR_2 = 1 + 2 - 1 + 7.0; // incompatible type expected int, found float
// const int:Signed8 ERR_3 = 250 + 200 + 0x7; // out of int:Signed8 range, 457

// // Ex. 5
// const int[2] ARR_1 = [3, 1]; // list constructor - array, output = [3,1] & readonly
// const int[10] ARR_2 = [0x2, 1, ...ARR_1, 1, 1]; // Fill members, output = [2,1,3,1,1,1,0,0,0,0] & readonly

// // Ex. 6
// const [string, int] TUPLE_1 = ["ABC", 1]; // list constructor - tuple, output = ["ABC",1] & readonly
// const [string, int, string, int, int, float] TUPLE_2 = ["ABC", 1, ...TUPLE_1, 1, 1]; // output - ["ABC",1,"ABC",1,1,1.0f] & readonly
// const TUPLE_3 = ["ABC", 1, ...TUPLE_2, 1, 1]; // output - ["ABC",1,"ABC",1,"ABC",1,1,1.0f,1,1] & readonly

// // Ex. 7
// const map<int|string> MAP_1 = {"aa": 1 + 2, "bb": "12"}; // mapping constructor expr, output - record {| 3 aa; "12" bb; |} & readonly
// const REC_1 = {"aa": 1 + 2, "bb": "12"}; // output - record {| 3 aa; "12" bb; |} & readonly



// // Basic Types

// // Numeric Valid

// const int SEVEN = 7;  // output - 7
// const int NUM1 = 1 + TWO - 1 + SEVEN; // output - 9
// const int TWO = 2; // output - 2
// const float NUM2 = 1 + 2 - 0x1. + 7f; // output - 9.0f
// const decimal NUM3 = 1 + 2 - 1.0 + 7d; // output - 9.0d
// const NUM4 = 1 + 2; // output - 3
// const NUM5 = 1.0 + 2 * 3.0; // not supported for finite type // output -
// const NUM6 = 1d + 2 * 3d; // output -

// const float NUM7 = (+1) + (-2); // output - -1.0f
// const decimal NUM8 = 1 * -2.0; // output - -2.0d

// const NUM9 = 1 * 2.0; // output -
// const NUM10 = 1.0 * 2 / 3.0; // output -
// const NUM11 = 1 * 2 / 3d;  // output -

// const byte NUM12 = 400 + 400 - 1000 + 300; // output - 100
// const int:Signed8 NUM13 = 250 + 200 - 450 + 0x7; // output - 7

// const int|float|decimal NUM14 = 1 + 2 - 1 + 7; // output - 9
// const float|decimal NUM15 = 1 + 2 - 0x1. + 7f; // output - 9.0f
// const 500|500.0 NUM16 = 100 + 400; // output - 500
// const 500|500.0 NUM17 = 100 + 400.0; // output - 500.0f
// const byte|int:Signed8 NUM18 = 100; // output - 100

// // Numeric Invalid

// const int ERR_NUM1 = 1 + 2 - 1 + 7.0;
// const float ERR_NUM2 = 1 + 2 - 0x1. + 7d;
// const decimal ERR_NUM3 = 1 + 2 - 0x1.0 + 7d;
// const ERR_NUM4 = 1 + 2.0;
// const ERR_NUM5 = 1 + 2 * 3.0;
// const ERR_NUM6 = 1 + 2 * 3d;

// const byte ERR_NUM7 = 400 + 200 - 1000;
// const int:Signed8 ERR_NUM8 = 250 + 200 + 0x7;


// // Boolean

// const boolean BOOL1 = false; // output - false
// const boolean BOOL2 = !false; // output - true

// const BOOL3 = true; // output - true
// const BOOL4 = !true; // output - false


// /// Structured Types

// // Array & Tuple Type Valid

// const int[2] ARR1 = [3, 1]; // output - ([3,1] & readonly)
// const (string|int)[2] ARR2 = ["ABC", 1]; // Union static type // output - (["ABC",1] & readonly)
// const (string|int|float)[6] ARR3 = ["ABC", 1, ...ARR2, 1, 1]; // Spread member // output - (["ABC",1,"ABC",1,1,1] & readonly)
// const int[10] ARR4 = [0x2, 1, ...ARR1, 1, 1]; // Fill members // output - ([2,1,3,1,1,1,0,0,0,0] & readonly)
// const int[10][2] ARR5 = [[]]; // Fill members // output - ([[0,0],[0,0],[0,0],[0,0],[0,0],[0,0],[0,0],[0,0],[0,0],[0,0]] & readonly)

// const [int, float] TUPLE1 = [3, 1]; // output - ([3,1.0f] & readonly)
// const [string, int] TUPLE2 = ["ABC", 1]; // output - (["ABC",1] & readonly)
// const [string, int, string, int, int, float] TUPLE3 = ["ABC", 1, ...TUPLE2, 1, 1]; // Spread member // output - (["ABC",1,"ABC",1,1,1.0f] & readonly)
// const [string, int, string, int, string, int, int, float, decimal, int] TUPLE4 = ["ABC", 1, ...TUPLE3, 1, 1]; // Fill members // output - (["ABC",1,"ABC",1,"ABC",1,1,1.0f,1d,1] & readonly)
// const [string, int, string, (), decimal, boolean[2][2], [int?, boolean, string[2], int[], any, anydata, record {}, record {| int a; 2 b|}, int:Signed16, int:Unsigned32] TUPLE5 = ["12", 1];  // output - (["12",1,"",(),0d,[[false,false],[false,false]],[(),false,["",""],[],(),(),record {| |},record {| 0 a; 2 b; |},0,0]] & readonly)
// const [1, "1", null, null|1, "2"|null, "1", 1, ()|1, record {| int a; 2 b|}] TUPLE6 = []; // output - ([1,"1",(),(),(),"1",1,(),record {| 0 a; 2 b; |}] & readonly)


// // Array & Tuple Type Invalid

// const string[2] ERR_ARR1 = [3, 1]; // incompatible types
// const int[1] ERR_ARR2 = [3, 1]; // incompatible types, size mismatch
// const [int, float] ERR_TUPLE2 = [3, 1, 3]; // incompatible types, size mismatch
// const [1|2] ERR_TUPLE3 = []; // no fill members
// const int[2]|[int, int] ERR_TUPLE4 = []; // ambiguous types


// // Maps and records Valid

// const map<int|string> MAP1 = {"aa": 1 + 2, "bb": "12"}; // output - (record {| 3 aa; "12" bb; |} & readonly)
// const map<int|string|map<int|string>> MAP2 = {"aa": 1 + 2, "bb": "12", "cc": {"aa2": 1 + 2, "bb2": "12"}}; // output - (record {| 3 aa; "12" bb; record {| 3 aa2; "12" bb2; |} cc; |} & readonly)
// const string ABC = "abc";
// const map<int|string> MAP3 = {[ABC]: ABC, [ABC+ABC]: "aaa", "abc": 1000, "abcabc": 1111, "a": 1 + 2, "b": "12", ABC, ...MAP1, [ABC+ABC+ABC]: "abcde"}; // output - (record {| "abc" abc; "aaa" abcabc; 3 a; "12" b; "abc" ABC; 3 aa; "12" bb; "abcde" abcabcabc; |} & readonly)

// const record{|int aa; string bb;|} REC1 = {"aa": 1 + 2, "bb": "12"}; // output -
// const record{|int aa; string...;|} REC2 = {"aa": 1 + 2, ABC}; // output -
// const record{} REC3 = {"cc": 1 + 2, ...REC1}; // output -

// // Maps and records Invalid

// const map<int|string> ERR_MAP1 = {"aa": 1 + 2.0, "bb": "12"};
// const map<int>|record{int...} ERR_MAP2 = {"aa": 1};
// const map<int|string> ERR_MAP1 = {"aa": 1 + 2.0, "bb": "12"};

// int DEF = 123;
// const record{|int aa; (string|int)...;|} REC2 = {"aa": 1 + 2, DEF}; //
// const record{} REC3 = {"aa": 1 + 2, ...REC1}; //


//type DEF_REC record{| INT a = 11;|};

// class Person {
//     public int age = 10;
//     public string name = "sample name";
//     int year = 50;
//     DEF_REC month = {a: 11};
// }

// Person P = new ();

// type DEF_REC record{| INT a = 11;|};

// class Person2 {
//     public int age = 10;
//     public string name = "sample name";
//     INT year = 50;
//     string month = "february";
// }

// type INT int;

// const record{| int a = 10|} REC_DEF_CONST = {};

// record{| int a = 10;|} REC_DEF_GLOBAL = {};












// // MB<:MI
// // // MB<:RIC
// // // MI<:RIC
// // // RC<:MI
// // // RC<:RIC
// // // RIC<:MI
// // //const boolean BBBBB = !false;
// // //const string ABC = "1" + "2";
// // //const int ABCABC = 1 + 2;
// // //const float ABCABC2 = 1 + 2;
// // //const decimal ABCABC3 = 1 + 2;
// // //const int|float ABCABC4 = 1 + 2+ (-2.1);
// // const ABCABC5 = 1 + 2+ (-2.1);
// // const int AAAAA = -1;

// // const int ABC2 = AAAAA;
// // const [string, int] ARR_ = [ABC, 1];
// // const [string, int, string, int, int, float] ARR2_ = [ABC, 1, ...ARR_, 1, 1];
// // const [string, int, string, int, string, int, int, float, decimal, int] ARR3_ = [ABC, 1, ...ARR2_, 1, 1];
// // //const [string, int, string, (), decimal, boolean[2][2], [int, boolean, string[2], int[]] ARR2_ = ["12", 1];
// // const map<int|string> Rec11 = {"aa": 1 + 2, "bb": "12"};
// // const map<int|string|map<int|string>> Rec12 = {"aa": 1 + 2, "bb": "12", "cc": {"aa2": 1 + 2, "bb2": "12"}};
// // const map<int|string> Rec10 = {[ABC]: ABC, [ABC+ABC]: "aaa", "abc": 1000, "abcabc": 1111, "a": 1 + 2, "b": "12", ABC, ...Rec11, [ABC+ABC+ABC]: "abcde"};
// // //const decimal|int|boolean Bin = 1 + 0x2;
// // const Bin2 = 1 + 2 + 1 + 2 + 1 + 21 + 1 + 1 + 1 + 1 + 1;
// // const decimal|int|boolean Bin3 = 1 + 2d;
// // const decimal|int|boolean Bin4 = 1.0 + 12;
// // const decimal|int|boolean Bin5 = 1d + 0x2;
// // const decimal|int|boolean Bin6 = 1.0 * 0x2;
// // const (record {|decimal a = 11;|})|int|string|(record {|decimal b;|}) Rec0 = {"a": 1 + 2};
// // const decimal|int Bin = 1 + 2;
// // const record{|int a;|}|decimal Bin2 = 1d + 2.0f;
// // const float Bin3 = 1 + 2;
// // const float Bin4 = 1 + 2.0;
// // const string CONST_STR = "A";
// // const CONST_FLT2 = 1.0 + 1.0;
// // const string CONST_STR2 = "A" + CONST_STR;


// // //type R1_R2 record {|
// // //    int[] intArr;
// // //    R2_R3 r2;
// // //    float[] floatArr;
// // //|} & R6;

// // //type R2_R3 record {|
// // //    int[] intArr;
// // //    string[] strArr;
// // //    R3_R1 r3;
// // //|} & R5;

// // //type R3_R1 record {|
// // //    int[] intArr;
// // //    R1_R2 r1;
// // //|};

// // //type R5 record {
// // //    int[] intArr;
// // //};

// // //type R6 record {
// // //    int[] intArr;
// // //};

// // //type REC3 record{|int x; REC4 y;|} & readonly;
// // //type REC4 record{|int x; REC3 y;|} & readonly;

// // //type X [int[]|stream<int, error?>] & readonly;

// // // type R1_R2 record {|
// // //     int[] intArr;
// // //     R2_R3 r2;
// // //     float[] floatArr;
// // // |} & readonly;

// // // type R2_R3 record {|
// // //     int[] intArr;
// // //     string[] strArr;
// // //     R3_R1 r3;
// // // |} & readonly;

// // // type R3_R1 record {|
// // //     int[] intArr;
// // //     R1_R2 r1;
// // // |};

// // type ZZ4 [string|ZZ2] & readonly;
// // type ZZ2 [int[]|ZZ3[]] & readonly;
// // type ZZ3 [string[]|ZZ4];

// // //const float CONST_FLT = 1 + 1.0 + "A";
// // //const CONST_FLT2 = 1 + 1.0 + "A";
// // //const ZZ4 CONST_TUPLE = [1] + 1.0 + "A";





// // //type REC3 record{|int x; REC3 y;|};
// // //type REC4 record{int x; REC4 y;};

// // //type ARR3 (int|ARR3)[2][1];
// // //type ZZ [string|ZZ];
// // //type MI2 map<int|MI2>;




// // //type ARR2 (int|ARR2)[];
// // //type Union CYC|[ZZ|Union];
// // //type CYC [int|CYC];
// // //type MI map<int|MI>;
// // //type REF MI;
// // //type REC record{| int a; REC b;};
// // //type ARR (int|ARR)[];
// // //type TP [AA, CC , MI];
// // //type BB int;
// // //type CC BB;
// // //type MI map<AA>;
// // //type AA 5.7f;
// // //type MB map<byte>;
// // //type RIC record {| int...; |};
// // //type RC record {| int i; |};




















// // public function print(any|error value) = @java:Method {
// //     'class: "org.ballerinalang.test.sandbox.DummyTest",
// //     name: "print"
// // } external;
