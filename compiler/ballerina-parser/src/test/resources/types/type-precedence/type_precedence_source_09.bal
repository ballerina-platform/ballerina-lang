type custom1 Foo & distinct Bar[2]?|distinct Baz? & Qux?;

type custom2 int[]? & distinct distinct Bar[]?|string? & ()|nill?;

type custom3 distinct Foo? & readonly? & int?|Baz[]? & Qux[a:b]? & boolean|distinct float?;

Foo? & readonly|distinct int[*]?|string? & readonly[]? custom4;

var output = from Foo[]? & distinct readonly?|int[]? & readonly? person in personList
    join Foo[]?|() & distinct json?[]? department in deptList on person.id equals dept.id
    select rec;
