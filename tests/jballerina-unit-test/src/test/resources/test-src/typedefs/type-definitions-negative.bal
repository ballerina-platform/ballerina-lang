type T1 A[];

type T2 [B, C];

type T3 map<D>;

type T4 E;

type T5 record { F f; };

type T6 object { G g; };

type T7 int[]|A[]|[B, C]|map<string>|map<D>|E|int|record { F f; }|object { G g; }|error;

type T8 [int[], A[], [B, C], map<string>, map<D>, E, int, record { F f; }, object { G g; }, error];
