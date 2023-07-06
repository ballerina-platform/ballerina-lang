type customType1 function () returns Foo|Bar;

type customType2 function () returns Foo?|Bar;

type customType3 function () returns Foo[]|Bar;

type customType4 function () returns Foo[]?|Bar;

type customType5 function () returns Foo?[]?|Bar;

type customType6 function () returns Foo?[]?|Bar[];

type customType11 function () returns Foo & Bar;

type customType12 function () returns Foo? & Bar;

type customType13 function () returns Foo[] & Bar;

type customType14 function () returns Foo[]? & Bar;

type customType15 function () returns Foo?[]? & Bar;

type customType16 function () returns Foo?[]? & Bar[];

type customType21 function () returns Foo|Bar & Baz;

type customType22 function () returns Foo & Bar|Baz;

type customType23 function () returns Foo & Bar[]|Baz;

type customType24 function () returns Foo & Bar & Baz?;
