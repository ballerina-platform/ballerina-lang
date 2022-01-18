// EmptyIntArray<:IntArray
// N<:IntArray
// LargeArray<:IntArray
// LargeArray2<:IntArray
// N<:EmptyIntArray
// N<:Array7x2x5
// N<:LargeArray
// N<:LargeArray2
// N<: INT

type IntArray int[];

type EmptyIntArray int[0];

type Array7x2x5 int[7][2][5];

type N never;

public const int VALUE = 922337;
public const int VALUE_M_1 = 922336;

type LargeArray int[VALUE];

type LargeArray2 int[VALUE_M_1];

type INT int;
