// Array7x2x5<:Array7OfArray2OfInt5
// Array7OfArray2OfInt5<:Array7x2x5

type Array2OfInt5 Int5[2];

type Array7OfArray2OfInt5 Array2OfInt5[7];

type Array7x2x5 int[7][2][5];

type Int5 int[5];
