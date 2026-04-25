# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Swing JSON Editor is a desktop Java/Swing application for editing JSON and YAML files with a tree-based UI and code editor. It targets macOS and packages as a native DMG.

## Commands

```bash
mvn clean compile      # Compile
mvn test               # Run tests
mvn clean package      # Full build + create DMG in target/dist/
```

To run a single test class: `mvn test -Dtest=ClassName`

## Architecture

The app uses MVC with an AST-based document model and a dual-pane editor (tree view + code editor).

**Key layers:**

- **`SwingJsonEditorApp`** — Entry point. Sets macOS system properties, applies FlatMacLightLaf theme, launches `MainFrame` on the EDT.
- **`controller/EditorManager`** — Central controller. Manages open editors/tabs, routes actions, and mediates between model and views.
- **`model/EditorDocument`** — Document state: raw text content, parsed `AstNode` tree, dirty tracking (CRC32), and `DocumentType` (JSON/YAML). Fires `PropertyChangeEvent`s when state changes.
- **`ast/`** — AST parsing and traversal. `FormatParser` interface has two implementations: `JsonParserAdapter` (Jackson) and `YamlParserAdapter` (SnakeYAML). Parsed output is a tree of `AstNode`s.
- **`ui/editor/EditorPanel`** — The main split-pane editor, composed of:
  - `tree/TreePanel` — Swing `JTree` rendered from `AstNode` via `TreeBuilder`. `AstIntervalIndex` maps tree nodes back to text ranges for bidirectional sync.
  - `code/CodePanel` — RSyntaxTextArea-based code editor with syntax highlighting and `StatusBar`.
- **`actions/`** — Action handlers (file open/save/new, edit, find). `ActionManager` tracks enabled/disabled state.
- **`ui/tabs/EditorTabbedPane`** — Tabbed container supporting drag-and-drop tab reordering.

**Data flow (code edit → tree):** User types in `CodePanel` → `EditorDocument.setContents()` → `FormatParser` produces `AstNode` tree → `TreeBuilder` converts to `JTree` model → `TreePanel` repaint.

**Data flow (tree edit → code):** User edits tree node → AST is mutated → `AstPrinter` serializes back to string → `CodePanel` updated.

## Key Libraries

| Library | Purpose |
|---|---|
| FlatLAF 3.4 | macOS-native Swing theme |
| Jackson 2.17 | JSON parsing |
| SnakeYAML 2.7 | YAML parsing |
| RSyntaxTextArea 3.6 | Syntax-highlighted code editor |
| Ikonli / FontAwesome5 | Icons |
| JSpecify 1.0 | `@Nullable`/`@NonNull` annotations |
| Kotlin stdlib 2.2 | Used alongside Java sources |

## Release Process

1. Update `CHANGELOG.md` with release date
2. Remove `-SNAPSHOT` from version in `pom.xml`
3. Commit: `git commit -m "Release version X.X.X"`
4. Tag: `git tag -a -m "Tag version X.X.X" vX.X.X`
5. Push tag: `git push origin tag vX.X.X`
6. Run `mvn clean package` — DMG is produced in `target/dist/`
7. Create GitHub release with CHANGELOG content and attach the DMG
8. Add next `-SNAPSHOT` version to `pom.xml` and update `CHANGELOG.md`
