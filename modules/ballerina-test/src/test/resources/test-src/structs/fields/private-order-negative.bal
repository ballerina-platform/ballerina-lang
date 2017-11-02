struct TestInvalidOrder {
    string p;
    private int q;
    string r;
    float s;
    private boolean t;
    blob u;
}


struct TestAnonymous1 {
    struct {
        string x;
        int y;
    } p;
    string q;
    private struct {
        float a;
        boolean b;
    } r;
    string s;
}

struct TestAnonymous2 {
    struct {
        string a;
        private string x;
        int y;
    } p;
    string q;
    float r;
}

struct CorrectStruct {
    private struct {
        private string x;
        private int y;
    } p;
    private string q;
    private struct {
        private float a;
        private boolean b;
    } r;
    private string s;
}
