public function partition(int[] arr) returns int[][] {
    int[][] partitioned = [];
    int counter = 0;
    int? prev = ();
    foreach var curr in arr {
        if curr != prev {
            if prev is () {
                counter += 1;
            } else {
                partitioned.push([prev, counter]);
                counter = 0;
            }
            prev = curr;
            counter = 1;
        } else {
            counter += 1;
        }
    }
    partitioned.push([arr[arr.length()-1], counter]);
    return partitioned;    
}
public function zip(int[] a, int[] b) returns int[][] {
    final int aLastIndex = a.length() - 1;
    final int bLastIndex = b.length() - 1;

    if aLastIndex < bLastIndex {
        var a_ = a.clone();
        var filler = a[aLastIndex];
        foreach var _ in (aLastIndex + 1) ... bLastIndex {
            a_.push(filler);
        }
        int[][] zipped = [];
        foreach var i in 0 ... bLastIndex {
            zipped.push([a_[i], b[i]]);
        } 
        return zipped;
    } else if bLastIndex < aLastIndex {
        var b_ = b.clone();
        var filler = b[bLastIndex];
        foreach var _ in (bLastIndex + 1) ... aLastIndex {
            b_.push(filler);
        }
        int[][] zipped = [];
        foreach var i in 0 ... aLastIndex {
            zipped.push([a[i], b_[i]]);
        } 
        return zipped;
    } else {
        int[][] zipped = [];
        foreach var i in 0 ... aLastIndex {
            zipped.push([a[i], b[i]]);
        } 
        return zipped;
    }
}