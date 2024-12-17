isolated int moduleCount = 0;

function name(int count) returns boolean {
    if count >= moduleCount / 2 {
        return true;
    }
    return false;
}
