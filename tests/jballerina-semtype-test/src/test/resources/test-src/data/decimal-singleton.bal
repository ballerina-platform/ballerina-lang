// Zero<:DECIMAL
// One<:DECIMAL
// Minus<:DECIMAL
// ZO<:DECIMAL
// ZOM<:DECIMAL
// ZO<:ZOM
// Zero<:ZO
// One<:ZO
// Zero<:ZOM
// One<:ZOM
// Minus<:ZOM

type Zero 0d;
type One 1d;
type Minus -1d;
type ZO Zero|One;
type ZOM Zero|One|Minus;
type DECIMAL decimal;
