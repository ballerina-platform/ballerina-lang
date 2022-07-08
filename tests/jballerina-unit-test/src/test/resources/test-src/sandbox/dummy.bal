// MB<:MI
// MB<:RIC
// MI<:RIC
// RC<:MI
// RC<:RIC
// RIC<:MI

type REC record{| int a; CC b;};
type ARR (int|string)[];
type TP [AA, CC , MI];
type BB int;
type CC BB;
type MI map<AA>;
type AA 5.7f;
type MB map<byte>;
type RIC record {| int...; |};
type RC record {| int i; |};




















public function print(any|error value) = @java:Method {
    'class: "org.ballerinalang.test.sandbox.DummyTest",
    name: "print"
} external;
