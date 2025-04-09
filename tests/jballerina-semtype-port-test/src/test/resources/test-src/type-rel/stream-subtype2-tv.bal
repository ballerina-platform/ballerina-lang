// @type I < J
// @type I < J1
// @type I < J2
type I stream<int, ()>;

// @type S < J
// @type S < J1
// @type S < J2
type S stream<string,()>;

type T int|string|();
// @type J = J1
type J stream<T>;
type J1 stream<T, ()>;

// @type J < J2
// @type J1 < J2
type J2 stream<T, T>;
type J3 stream<T, error>;
