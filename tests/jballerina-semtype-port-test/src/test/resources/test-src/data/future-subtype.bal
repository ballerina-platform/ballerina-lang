// FI<:FU1
// FI<:FU2
// FS<:FU1
// FS<:FU2
// FU1<:FU2

type FI future<int>;
type FS future<string>;
type FU1 future<int>|future<string> ;
type FU2 future<int|string> ;
