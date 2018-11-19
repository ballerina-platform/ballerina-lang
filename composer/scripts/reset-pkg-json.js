const { forEachSubModule, runGitCommand } = require('./common');

forEachSubModule((modPath) => {
    // Undo changes to package-lock files. These changes mostly happen
    // because of differences between npm versions
    runGitCommand(["checkout", "--", "package-lock.json"], modPath);
});
