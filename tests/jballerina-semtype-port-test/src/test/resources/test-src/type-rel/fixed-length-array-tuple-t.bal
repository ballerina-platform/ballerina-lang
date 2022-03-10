type Int1 int[1];
type Int2 int[2];

// @type IntT = Int1
type IntT [int];

// @type IntIntT = Int2
type IntIntT [int, int];

// @type IntIntRT < IntIntT
// @type IntIntRT < Int2
type IntIntRT readonly & [int, int];

// @type Int2R = IntIntRT
type Int2R readonly & int[2];

// @type Int = IntIntT[0]
type Int int;

// @type Int = Int2Intersection[0]
// @type Int = Int2Intersection[1]
type Int2Intersection IntIntT & int[2];

// @type Int2Intersection = Int2AnyArrayIntersection
// @type Int = Int2AnyArrayIntersection[0]
// @type Int = Int2AnyArrayIntersection[1]
type Int2AnyArrayIntersection IntIntT & any[];
