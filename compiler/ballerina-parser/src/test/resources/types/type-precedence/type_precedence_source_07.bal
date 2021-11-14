type custom1 Foo & Bar[2]?|Baz[]? & Qux?[]??;

type custom2 int[]? & Bar?[]?|string & ()?|nill;

type custom3 Foo? & readonly & int?|Baz[]? & Qux[a:b]? & boolean?|float;

Foo & readonly|int?[*]?|string? & readonly[]? custom4;
