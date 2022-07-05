type customType readonly & int|Baz[] & Qux[mod1:app] & boolean|distinct float|
function () returns Foo & distinct Bar|Baz & Qux[0] &
function () returns int[] & distinct distinct Bar[]|string[];

type customType2 readonly & int|Baz[]? & Qux[mod1:app]? & boolean|distinct float?|
function () returns Foo? & distinct Bar?|Baz? & Qux[0]? &
function () returns int[]? & distinct distinct Bar?[]?|string[]?;
