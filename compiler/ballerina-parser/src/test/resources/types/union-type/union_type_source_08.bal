public function main() {
    -2 t1;
    -2|0 t2;
    (-2|0) t3;
    1|2|0 t4;
    (1|2|0) t5;
    +1|0|-2 t6;
    (+1|0|-2) t7;
    "apple"|"orange" t8;
    ("apple"|"orange") t9;
    true|false t10;
    (true|false) t11;

    -2 t1 = -2;
    -2|0 t2 = -2;
    (-2|0) t3 = 0;
    1|2|0 t4 = 1;
    (1|2|0) t5 = 2;
    +1|0|-2 t6 = 1;
    (+1|0|-2) t7 = +1;
    "apple"|"orange" t8 = "apple";
    ("apple"|"orange") t9 = "apple";
    (true|false) t10 = true;
    (true|false) t11 = true;
}
