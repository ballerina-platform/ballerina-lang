const { forEachSubModule, runNpmCommandSync, runGitCommand } = require('./common');

forEachSubModule((modPath) => {
    runNpmCommandSync(["i"], modPath);
    runNpmCommandSync(["run", "build"], modPath);
    // Undo changes to package-lock files. These changes mostly happen
    // because of differences between npm versions
    runGitCommand(["checkout", "--", "package-lock.json"], modPath);
});
