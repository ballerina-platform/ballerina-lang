function main (string[] args) {
    if (args.length == 0) {
        errors:Error err = {msg:"No arguments provided."};
        thro<caret>w err;
    } else if (args.length > 5) {
        errors:Error err = {msg:"Only 4 arguments required."};
        throw err;
    }
}
