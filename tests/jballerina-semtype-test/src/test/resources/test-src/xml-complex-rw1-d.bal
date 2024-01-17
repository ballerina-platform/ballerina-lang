type X xml;

type N xml<never>;
type T xml:Text;
type E xml:Element;
type P xml:ProcessingInstruction;
type C xml:Comment;
type XE xml<E>;
type XP xml<P>;
type XC xml<C>;

type S T|E|P|C;

// @type NonEmptyS < S
// @type NonEmptyS <> T
// @type NonEmptyS <> N
// @type E < NonEmptyS
// @type P < NonEmptyS
// @type C < NonEmptyS
type NonEmptyS S & !N;

// @type NonEmptyS < UX
type UX XE|XP|XC|T;

// @type XNonEmptyS = X
type XNonEmptyS xml<NonEmptyS>;
