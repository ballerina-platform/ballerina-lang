public function concat(string[] a, string[] b) returns string[] {
    var c = a.clone();
    foreach var item in b {
        c.push(item);
    }
    return c;
}

public function cut(string value, string separator = ":") returns ([string, string]) {
    int? cutIndex = value.indexOf(separator, 0);
    if cutIndex is int {
        int separatorLength = separator.length();
        string head = value.substring(0, cutIndex);
        string tail = value.substring(cutIndex + separatorLength);
        return [head, tail];
    } else {
        return [value, ""];
    }
}

public function joinToString(string[] arr) returns string {
    string accu = "";
    foreach var str in arr {
        accu = string:concat(accu, str);
    }
    return accu;
}
public function splitToChar(string value) returns string[] {
    string[] accu = [];
    foreach var char in value {
        accu.push(char);
    }
    return accu;
}
public function split(string value, string separator = ",") returns string[] {
    string[] accu = [];
    var [head, tail] = cut(value, separator);
    accu.push(head);
    if tail.length() > 0 {
        accu = concat(accu, split(tail, separator));
    }
    return accu;
}

# Convert binary number in string representation to integer
# 
# Assumes valid input.
#
# + binary - string representation of binary number
# + return - integer value
public function stringBinaryNumberToInt(string binary) returns int {
    string reversed = "";
    {
        int i = binary.length() - 1;
        while i >= 0 {
            reversed += binary[i];
            i -= 1;
        }
    }

    int num = 0;
    {
        int i = 0;
        foreach var bit in reversed {
            if bit == "1" {
                num += power(2, i);
            }
            i += 1;
        }
    }

    return num;
}

public function str2int(string value) returns int {
    return checkpanic 'int:fromString(value);
}

public function unpad(string value) returns string {
    boolean skip = false;
    string str = "";
    foreach var char in value.trim() {
        if char == " " && !skip {
            str += char;
            skip = true;
        } else if char == " " && skip {
        } else {
            str += char;
            skip = false;
        }
    }
    return str;
}