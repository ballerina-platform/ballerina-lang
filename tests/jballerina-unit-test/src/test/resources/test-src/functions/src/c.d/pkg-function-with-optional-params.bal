
public function functionWithAllTypesParams(public int a, public float b, public string c = "John", public int d = 5,
                                            public string e = "Doe", int... z)
            returns [int, float, string, int, string, int[]] {
    return [a, b, c, d, e, z];
}
