int recurs1 = 0;
int recurs2 = 0;

public function chain(int chainBreak, boolean startFunc) {
    if (chainBreak == 1 && !startFunc) {
        return;
    }

    recurs1 += 10;
    chain2(chainBreak, false);
}

public function chain2(int chainBreak, boolean startFunc) {
    if (chainBreak == 2 && !startFunc) {
        return;
    }

    recurs2 += 10;
    chain3(chainBreak, false);
}

public function chain3(int chainBreak, boolean startFunc) {
    if (chainBreak == 3 && !startFunc) {
        return;
    }

    chain(chainBreak, false);
}

public function getValues() returns [int, int] {
    return [recurs1, recurs2];
}
