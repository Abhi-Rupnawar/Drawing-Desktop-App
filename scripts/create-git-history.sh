#!/usr/bin/env bash
#
# Creates the Git history for this project as a sequence of meaningful commits,
# using your own Git identity.
#
# Run once, from the repository root:
#
#     ./scripts/create-git-history.sh
#
# It refuses to run if a .git directory already exists, so it can never rewrite
# an existing history.

set -euo pipefail

if [ -d .git ]; then
    echo "A .git directory already exists. Refusing to touch existing history." >&2
    exit 1
fi

if [ ! -f pom.xml ]; then
    echo "Run this script from the repository root (pom.xml not found)." >&2
    exit 1
fi

git init --quiet

commit() {
    local message="$1"
    shift
    git add -- "$@"
    git commit --quiet -m "$message"
    echo "  committed: $message"
}

SRC=src/main/java/com/example/drawingapp
TEST=src/test/java/com/example/drawingapp

commit "Set up Maven project with Spring Boot, JavaFX and Jackson" \
    pom.xml .gitignore src/main/resources/application.properties

commit "Add geometry value types and bounds helpers" \
    $SRC/model/Point.java $SRC/model/Bounds.java $SRC/util/BoundsUtil.java

commit "Add drawing object model for rectangle, ellipse, line and text" \
    $SRC/model/enums $SRC/model/DrawingObject.java $SRC/model/StyleDefaults.java \
    $SRC/model/RectangleObject.java $SRC/model/EllipseObject.java \
    $SRC/model/LineObject.java $SRC/model/TextObject.java $SRC/model/DrawingDocument.java

commit "Add document, selection and view transform services" \
    $SRC/util/ChangeSupport.java $SRC/service/DrawingService.java \
    $SRC/service/SelectionService.java $SRC/service/ViewTransformService.java

commit "Add JSON persistence with validated, whitelisted deserialization" \
    $SRC/exception/DrawingFileException.java $SRC/repository/DrawingFileRepository.java \
    $SRC/service/FileService.java $SRC/config/ApplicationConfig.java

commit "Add canvas controller for shape creation, selection and dragging" \
    $SRC/controller/TextInputProvider.java $SRC/controller/DrawingController.java

commit "Add document controller for new, open and save commands" \
    $SRC/controller/DocumentController.java

commit "Add canvas renderer with grid and zoom-aware selection indicator" \
    $SRC/ui/CanvasRenderer.java

commit "Add drawing canvas with mouse interaction, wheel zoom and panning" \
    $SRC/ui/DrawingCanvas.java

commit "Add toolbar and status bar" \
    $SRC/ui/ToolbarActions.java $SRC/ui/DrawingToolbar.java $SRC/ui/StatusBar.java

commit "Add dialogs for file selection, text input and error reporting" \
    $SRC/ui/DialogService.java $SRC/ui/UnsavedChangesChoice.java

commit "Wire main view with keyboard shortcuts and unsaved-changes handling" \
    $SRC/ui/MainView.java

commit "Launch JavaFX application with Spring dependency injection" \
    $SRC/DrawingApplication.java $SRC/ui/JavaFxApplication.java

commit "Add model and geometry unit tests" \
    $TEST/model $TEST/util

commit "Add service, persistence and interaction tests" \
    $TEST/service $TEST/controller

commit "Add README and project documentation" \
    README.md scripts

echo
echo "Done. Review with: git log --oneline"
