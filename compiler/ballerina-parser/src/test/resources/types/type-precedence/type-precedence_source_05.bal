Foo[] & int|float|readonly [] = expr1;

Foo[] & int & float|readonly [] = expr2;

int[*]|string & readonly[] [] = expr3;

readonly[]|int[*]|string & readonly[] [] = expr4;

Foo[] & readonly[]|int[*]|string & readonly[] [] = expr5;

Foo[]|int & readonly & float custom1 = expr6;

Foo[]|int & readonly|float custom2 = expr7;

Fruits[] & int & readonly & float custom3 = expr8;

Foo[]|int|float|byte|decimal|string custom4;

Fruits[] & Apple|Orange|Mango [] = expr9;

Fruits[] & Apple & Orange & Mango custom5;

Foo[]|Orange|int|readonly|Apple|string custom6;

Apple|Fruits[]|Orange|int[]|readonly|float|string custom7;

Apple|Orange|Mongo[] & Avocado & Cherry & Guava custom8;

Apple|Fruits[] & Orange custom9;

Apple|Orange & Fruits[] & Mango|Guava|Cherry custom10;
