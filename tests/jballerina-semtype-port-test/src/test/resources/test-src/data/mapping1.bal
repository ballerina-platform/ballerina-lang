// M1<:M
// M1<:M2
// M1<:N
// M2<:M
// M2<:M1
// M2<:N
// M<:M1
// M<:M2
// M<:N
// N<:M
// N<:M1
// N<:M2
type M map<any>;

type N map<any>;

type M1 map<any>|record {};

type M2 map<any>|record {};
