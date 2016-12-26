function lazyInitThrowArrayIndexOutOfBound() {
    int[] arr;
    int x;

    // This should throw an exception
    x = arr[0];
}

function lazyInitSizeZero() (string[]) {
    string[] names;

    return names;
}

// TODO
// 1) Array add value test
// 2) Array get value test
// 3) Array grow test
// 4) Array maximum size test
// 5) Array grow and size change test