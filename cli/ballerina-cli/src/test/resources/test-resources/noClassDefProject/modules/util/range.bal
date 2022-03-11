public function range(int begin, int end) returns int[] {
    int[] r = [];

    if end > begin {
        foreach var i in begin ... end {
            r.push(i);
        }
    } else if begin > end {
        int i = begin;
        while i >= end {
            r.push(i);
            i -= 1;
        }        
    } else {
        r.push(begin);
    }
    
    return r;
}