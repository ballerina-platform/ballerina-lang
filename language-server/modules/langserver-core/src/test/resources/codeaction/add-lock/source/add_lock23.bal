isolated int[] arr = [1, 2, 3];

function fn(int val) returns string {
    match val {
        1 => {
            return "one";
        }
        2 if arr[1] == 2 => {
            return "two";
        }
        _ => {
            return "other";
        }
    }
}
