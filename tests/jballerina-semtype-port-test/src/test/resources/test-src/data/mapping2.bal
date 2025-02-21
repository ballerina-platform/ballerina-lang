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
type M map<(1|2|3)>;

type N map<(1|2|3)>;

type M1 M|map<(1|2)>;

type M2 N|map<(1|2)>;
