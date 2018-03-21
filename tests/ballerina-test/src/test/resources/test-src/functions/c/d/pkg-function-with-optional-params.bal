package c.d;

public function functionWithAllTypesParams(int a, float b, string c = "John", int d = 5, string e = "Doe", int... z) 
            (int, float, string, int, string, int[]) {
    return a, b, c, d, e, z;
}
