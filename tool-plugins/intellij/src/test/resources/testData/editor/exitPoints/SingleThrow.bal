function main (string... args) {
    if (args.length == 0) {
        errors:Error err={msg:"No arguments provided."};
        thro<caret>w err;
    }
}
