// MB<:MI
// MB<:RIC
// MI<:RIC
// RC<:MI
// RC<:RIC
// RIC<:MI

type MI map<int>;
type MB map<byte>;
type RIC record {| int...; |};
type RC record {| int i; |};
