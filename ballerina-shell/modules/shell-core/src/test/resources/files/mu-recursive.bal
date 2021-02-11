function is_even(int n) returns boolean {
    if n == 0 {
        return true;
    }
    return is_odd(n - 1);
}

function is_odd(int n) returns boolean {
    if n == 0 {
        return false;
    }
    return is_even(n - 1);
}
