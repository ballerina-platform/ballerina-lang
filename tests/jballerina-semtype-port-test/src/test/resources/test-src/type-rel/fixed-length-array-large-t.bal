type IntArray int[];
type Int5 int[5];
type ISTArray (1|2|3)[];

public const int MAX_VALUE = 2147483637;
public const int MAX_VALUE_M_1 = MAX_VALUE - 1;

// @type LargeArray < IntArray
type LargeArray int[MAX_VALUE];

// @type LargeArray2 < IntArray
// @type LargeArray <> LargeArray2
type LargeArray2 int[MAX_VALUE_M_1];

// -@type Int5Intersection = Int5
type Int5Intersection int[5] & !LargeArray;

type Int10000 int[100000];

// -@type ISTArray < I10000A
type I10000A Int10000|(!Int10000 & IntArray);
