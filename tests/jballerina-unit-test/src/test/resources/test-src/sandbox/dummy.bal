// MB<:MI
// MB<:RIC
// MI<:RIC
// RC<:MI
// RC<:RIC
// RIC<:MI


//type R1_R2 record {|
//    int[] intArr;
//    R2_R3 r2;
//    float[] floatArr;
//|} & R6;

//type R2_R3 record {|
//    int[] intArr;
//    string[] strArr;
//    R3_R1 r3;
//|} & R5;

//type R3_R1 record {|
//    int[] intArr;
//    R1_R2 r1;
//|};

//type R5 record {
//    int[] intArr;
//};

//type R6 record {
//    int[] intArr;
//};

//type REC3 record{|int x; REC4 y;|} & readonly;
//type REC4 record{|int x; REC3 y;|} & readonly;

//type X [int[]|stream<int, error?>] & readonly;

// type R1_R2 record {|
//     int[] intArr;
//     R2_R3 r2;
//     float[] floatArr;
// |} & readonly;

// type R2_R3 record {|
//     int[] intArr;
//     string[] strArr;
//     R3_R1 r3;
// |} & readonly;

// type R3_R1 record {|
//     int[] intArr;
//     R1_R2 r1;
// |};

type ZZ4 [string|ZZ2] & readonly;
type ZZ2 [int[]|ZZ3[]] & readonly;
type ZZ3 [string[]|ZZ4];

const float CONST_FLT = 1 + 1.0 + "A";
const CONST_FLT2 = 1 + 1.0 + "A";
const ZZ4 CONST_TUPLE = [1] + 1.0 + "A";





//type REC3 record{|int x; REC3 y;|};
//type REC4 record{int x; REC4 y;};

//type ARR3 (int|ARR3)[2][1];
//type ZZ [string|ZZ];
//type MI2 map<int|MI2>;




//type ARR2 (int|ARR2)[];
//type Union CYC|[ZZ|Union];
//type CYC [int|CYC];
//type MI map<int|MI>;
//type REF MI;
//type REC record{| int a; REC b;};
//type ARR (int|ARR)[];
//type TP [AA, CC , MI];
//type BB int;
//type CC BB;
//type MI map<AA>;
//type AA 5.7f;
//type MB map<byte>;
//type RIC record {| int...; |};
//type RC record {| int i; |};




















public function print(any|error value) = @java:Method {
    'class: "org.ballerinalang.test.sandbox.DummyTest",
    name: "print"
} external;
