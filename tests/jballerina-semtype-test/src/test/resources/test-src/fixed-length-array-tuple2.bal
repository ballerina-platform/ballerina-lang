// IntIntT<:Int2
// IntIntRT<:Int2
// Int2<:IntIntT
// IntIntRT<:IntIntT

type Int2 int[2];

type IntIntT [int, int];

type IntIntRT readonly & [int, int];
