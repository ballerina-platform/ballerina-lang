// Int1<:IntT
// Int2<:IntIntT
// Int2R<:Int2
// Int2R<:IntIntRT
// Int2R<:IntIntT
// IntIntRT<:Int2
// IntIntRT<:Int2R
// IntIntRT<:IntIntT
// IntIntT<:Int2
// IntT<:Int1

type Int1 int[1];
type Int2 int[2];

type IntT [int];

type IntIntT [int, int];

type IntIntRT readonly & [int, int];

type Int2R readonly & int[2];

type Int int;
