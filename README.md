# swing-json-editor
JSON editor tool

# How to release
To create a release of the app follow these steps:
1. Checkout the `main` branch locally and make sure it is up-to-date (`git fetch` & `git pull`)
2. Set the release date in the CHANGELOG.md to today's date 
3. Set the definitive version in the pom.xml (remove -SNAPSHOT)
4. Commit the changes to `main` with the commit message: Release version X.X.X
5. Create a release tag: `git tag -a -m "Tag version X.X.X" vX.X.X`
6. Push to remote: `git push origin tag vX.X.X`
7. Run the following command to create a release DMG binary: `mvn clean package`
8. Create a new release in GitHub by going to https://github.com/DennisPallett/swing-json-editor/releases/new
   1. Select the just created tag
   2. Use title: X.X.X
   3. Use the contents of the CHANGELOG.md as release description
   4. Attach the release DMB binary
9. Add the new version to the CHANGELOG.md with UNRELEASED as release date
10. Update the version in the pom.xml to the next version with -SNAPSHOT.
11. Commit the changes to `main` with the commit message: Prepare for next development cycle
12. Push to remote: `git push`
