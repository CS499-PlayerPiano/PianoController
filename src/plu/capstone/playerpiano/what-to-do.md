# What to do for reorganization
## Plugins
- [ ] All plugins are called `Outputs`
- [ ] Plugin jar class loader is deleted and replaced with static single instances
- [ ] One `config/output.json` file that handles all outputs, each seperated into its own nested object
- [ ] All outputs are loaded and accessable at the start of the `SubProgram` class
- [ ] Move WebServer to the main controller, and not a output. Outputs can not queue things.

## Main Programs
- [ ] Main programs inharit from a single class called `SubProgram`
- [ ] All outputs are loaded and accessable from the `SubProgram` class
- [ ] Easy to create and configure sub program options via command line or config file
- [ ] Remove hacky plugin loading from MappingGenerator

## Misc
- [ ] Somehow seperate utility helper classes, and shared classes between SubPrograms
- [ ] Remove all static references to everything unless we move them to the SubProgram class
- [ ] Make it more defined the difference between a output and a SubClass is.
  - Outputs can only listen to note changes
  - SubPrograms can queue songs, listen to change IF DEFINED, but are more for running a program with defined outputs
