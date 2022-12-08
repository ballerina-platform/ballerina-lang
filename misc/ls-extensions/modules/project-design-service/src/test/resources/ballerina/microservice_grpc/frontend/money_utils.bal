// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

const map<string> logos = {
    "USD": "$",
    "CAD": "$",
    "JPY": "¥",
    "EUR": "€",
    "TRY": "₺",
    "GBP": "£"
};

# checks if specified value has a valid units/nanos signs and ranges.
#
# + m - object to be validated
# + return - Validity
isolated function isValid(Money m) returns boolean {
    return signMatches(m) && validNanos(m.nanos);
}

# checks if the sign matches
#
# + m - object to be validated
# + return - validity status
isolated function signMatches(Money m) returns boolean {
    return m.nanos == 0 || m.units == 0 || (m.nanos < 0) == (m.units < 0);
}

# checks if nanos are valid
#
# + nanos - nano input
# + return - validity status
isolated function validNanos(int nanos) returns boolean {
    return -999999999 <= nanos && nanos <= +999999999;
}

# checks if the money is zero
#
# + m - object to be validated
# + return - zero status
isolated function isZero(Money m) returns boolean {
    return m.units == 0 && m.nanos == 0;
}

# returns true if the specified money value is valid and is positive.
#
# + m - object to the validated
# + return - positive status
isolated function isPositive(Money m) returns boolean {
    return isValid(m) && m.units > 0 || (m.units == 0 && m.nanos > 0);
}

# returns true if the specified money value is valid and is negative.
#
# + m - object to the validated
# + return - negative status
isolated function isNegative(Money m) returns boolean {
    return isValid(m) && m.units < 0 || (m.units == 0 && m.nanos < 0);
}

# returns true if values l and r have a currency code and they are the same values.
#
# + l - first money object
# + r - second money object
# + return - currency type equal status
isolated function areSameCurrency(Money l, Money r) returns boolean {
    return l.currency_code == r.currency_code && l.currency_code != "";
}

# returns true if values l and r are the equal, including the currency.
#
# + l - first money object
# + r - second money object
# + return - currency equal status
isolated function areEquals(Money l, Money r) returns boolean {
    return l.currency_code == r.currency_code &&
l.units == r.units && l.nanos == r.nanos;
}

# negate returns the same amount with the sign negated.
#
# + m - object to be negated
# + return - negated money object
isolated function negate(Money m) returns Money {
    return {
        units: -m.units,
        nanos: -m.nanos,
        currency_code: m.currency_code
    };
}

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

isolated function renderMoney(Money money) returns string {
    return currencyLogo(money.currency_code) + money.units.toString() + "." + (money.nanos / 10000000).toString();
}

isolated function currencyLogo(string code) returns string {
    return logos.get(code);
}
