# sum adds two values.
#
# + l - first money object
# + r - second money object
# + return - sum money object
isolated function sum(Money l, Money r) returns Money {

    int nanosMod = 1000000000;

    int units = l.units + r.units;
    int nanos = l.nanos + r.nanos;

    if (units == 0 && nanos == 0) || (units > 0 && nanos >= 0) || (units < 0 && nanos <= 0) {
        // same sign <units, nanos>
        units += nanos / nanosMod;
        nanos = nanos % nanosMod;
    } else {
        // different sign. nanos guaranteed to not to go over the limit
        if units > 0 {
            units = units - 1;
            nanos += nanosMod;
        } else {
            units = units + 1;
            nanos -= nanosMod;
        }
    }

    return {
        units: units,
        nanos: nanos,
        currency_code: l.currency_code
    };
}

# slow multiplication operation done through adding the value to itself n-1 times.
#
# + m - money object to be multiplied
# + n - multiply factor
# + return - multiplied money object
isolated function multiplySlow(Money m, int n) returns Money {
    int t = n;
    Money out = m;
    while t > 1 {
        out = sum(out, m);
        t = t - 1;
    }
    return out;
}
