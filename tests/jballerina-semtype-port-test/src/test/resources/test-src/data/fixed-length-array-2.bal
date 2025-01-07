// LargeArray<:IntArray
// LargeArray2<:IntArray

public const int MAX_VALUE = 2147483637;
public const int MAX_VALUE_M_1 = 2147483636;

type IntArray int[];

// @type LargeArray < IntArray
type LargeArray int[MAX_VALUE];

// @type LargeArray2 < IntArray
// @type LargeArray <> LargeArray2
type LargeArray2 int[MAX_VALUE_M_1];

// @type Int5Intersection = Int5
//type Int5Intersection int[5] & !LargeArray;
