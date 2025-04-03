// I<:J
// I<:J1
// I<:J2
type I stream<int, ()>;

// S<:J
// S<:J1
// S<:J2
type S stream<string,()>;


// J<:J1
// J1<:J
type J stream<json>;
type J1 stream<json, ()>;

// J<:J2
// J1<:J2
type J2 stream<json, json>;
type J3 stream<json, error>;
