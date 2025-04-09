// EMPTY<:ALL
// EMPTY<:IntArray
// IS1<:ALL
// IS2<:ALL
// IS3<:ALL
// IntArray<:ALL

type IntArray int[];
type IS int|string;
type EMPTY [];
type IS1 IS[1];
type IS2 IS[2];
type IS3 IS[3];

type ALL EMPTY|IS1|IS2|IS3|[IS, IS, IS, IS, IS...];
