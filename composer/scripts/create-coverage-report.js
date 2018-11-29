var combine = require('istanbul-combine');

var opts = {
  dir: 'coverage',                       // output directory for combined report(s)
  pattern: '**/coverage/coverage-**.json',   // json reports to be combined 
  print: 'summary',                      // print to the console (summary, detail, both, none) 
  base:'sources',                        // base directory for resolving absolute paths, see karma bug
  reporters: {
    html: { /* html reporter options */ },
    cobertura: { /* etc. */ }
  }
};

combine.sync(opts);    
