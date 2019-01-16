
type RR record {
    int i;
};

type Obj object {
    int i;

    function __init() {
        self.i = 0;
    }
};

type RecordWithObj record {
   *RR;
    Obj obj;
};

Obj[] r = [];

RR[] arr2 = [];
RR[2] arr3 = [ {i: 0}, {i: 1}];
RR?[] arr2_ = [];

RecordWithObj[] rO = [];
RecordWithObj?[] rOptional = [];

RR[][] multiArr = [];
Obj[][] multiObjArr = [];

json[] jArr = [];

type FT 1|2|3;
type FTN 1|2|3|();

FT[] ar_ft = [1,2];
FT?[] ar_ft2 = [2,3];
FTN[] ar_ftn = [];
