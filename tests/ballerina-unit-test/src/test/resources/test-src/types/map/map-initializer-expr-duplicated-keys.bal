function duplicatedMapKeys() returns (map) {
    map dupKey = {key: "value-1", key: "value-2"};
    return dupKey;
}

function duplicatedMapKeysStrKey() returns (map) {
    map dupKey = {key: "value-1", "key": "value-2"};
    return dupKey;
}

function duplicatedMapKeysBothStrKey() returns (map) {
    map dupKey = {"key": "value-1", "key": "value-2"};
    return dupKey;
}
