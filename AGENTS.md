- always read the .md file in every folder you work in. if there is no .md file in this folder create it if it is in the backend folder for example then it should be called backend_developer_guide.md, if you are creating the .md file make sure it is written in a way that is geared towards developers, it should explain all the files and subfolders in that folder in a way that a developer that has never seen the code will know how to work with the code. these files should never be over 500 lines long.
- when changing any file or doing any edits make sure to update the .md file in that folder and in the ancestor folders as well. these .md files should never be longer than 500 lines long. the point of these .md files are to allow a new developer to understand everything without needing to read the code itself. they are not to document changes. only changes that require additional information for a developer that has never seen the code should be added into the .md files.
- no code file generated or edited should exceed 1000 lines of code ever. if while editing or creating a file, the file exceeds 1000 lines this file must be split up into multiple files.
- whenever creating a new file, think very carefully which folder this file should be put in, if needed make a new folder so that there is clear separation between folders.
- each folder should only have 1 .md file. never create summary files or visualization .md files.
- never build the android project allow the user to build and provide any build errors
- never mention legacy functionality or recent changes in the MD files they are only needed to get engineers into what is happening right now in the code.
- always ask if a commit message is good before commiting.
- backward compatibility is never a concern, always assume that everyone has the latest installed.
- after making code changes, ALWAYS run lint checks to ensure code quality:
  * Run `./gradlew lint` from the project root (requires build to pass first)
  * Fix all warnings introduced by your changes, including:
    - Unused imports
    - Unused parameters/variables
    - Locale issues (always use `String.format(Locale(...), format, args)` instead of `String.format(format, args)`)
    - Missing indices on foreign keys
  * Do not introduce new warnings - clean code is maintained code
