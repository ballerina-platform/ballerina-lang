type IntArr int[];

const int length = 3;

public const string[] l1 = ["a", "b", "c"];
public const [int, string] l2 = [1, "d"];
public const [int, [string, int]] l3 = [1, ["e", 2]];
public const int[][] l4 = [[1, 2, 3], [4, 5, 6]];
public const boolean[length] l5 = [true, false, true];
public const IntArr l6 = [1, 2, 3];
public const [int, string...] l7 = [1, "f", "g"];
public const (string|int)[][] l8 = [[1, "2", 3], [4, 5, 6]];
public const [(string[]|int[])...] l9 = [[1, 2, 3], ["4", "5", "6"]];
