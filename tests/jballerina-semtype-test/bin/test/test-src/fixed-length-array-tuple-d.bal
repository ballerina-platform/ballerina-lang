// @type Int = IntIntT[0]
type Int int;

// @type Int = Int2Intersection[0]
// @type Int = Int2Intersection[1]
type Int2Intersection IntIntT & int[2];

// @type Int2Intersection = Int2AnyArrayIntersection
// @type Int = Int2AnyArrayIntersection[0]
// @type Int = Int2AnyArrayIntersection[1]
type Int2AnyArrayIntersection IntIntT & any[];

type IntIntT [int, int];

type IntIntRT readonly & [int, int];
