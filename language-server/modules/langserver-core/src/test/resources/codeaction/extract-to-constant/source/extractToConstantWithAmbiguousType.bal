
public function main() {
    decimal val1 = 12;
    byte val2 = 11;
    float val3 = 12;
    decimal val4 = 1.32;
    string|byte val5 = 15;
    decimal & readonly val6 = 19;
    MyFloat val7 = 17;
    ParentFloat val8 = 18;

    fn(12, 12);
}

function fn(decimal val1, float val2) {

}

type MyFloat float;
type ParentFloat MyFloat;
