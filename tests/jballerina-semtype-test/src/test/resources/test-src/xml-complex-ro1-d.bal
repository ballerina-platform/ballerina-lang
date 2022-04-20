type X xml;
type RX readonly & X;

type N xml<never>;
type T xml:Text;
type E readonly & xml:Element;
type P readonly & xml:ProcessingInstruction;
type C readonly & xml:Comment;
type XE xml<E>;
type XP xml<P>;
type XC xml<C>;

type ReadOnlyFlat T|E|P|C;

// @type NonEmptyRoSingletons < ReadOnlyFlat
// @type NonEmptyRoSingletons <> T
// @type NonEmptyRoSingletons <> N
// @type E < NonEmptyRoSingletons
// @type P < NonEmptyRoSingletons
// @type C < NonEmptyRoSingletons
type NonEmptyRoSingletons ReadOnlyFlat & !N;

// @type NonEmptyRoSingletons < UX
type UX XE|XP|XC|T;

// @type XNonEmptyRoSingletons = RX
// @type XNonEmptyRoSingletons < X
type XNonEmptyRoSingletons xml<NonEmptyRoSingletons>;
