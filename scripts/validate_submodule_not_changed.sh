#!/bin/sh

SCRIPT_PATH="$( cd "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
SUBMODULE_NAME="$(basename "$(dirname "$SCRIPT_PATH")")"

echo "Detected solace-integration-test-support submodule name: ${SUBMODULE_NAME}"

if [ -z "$(git status -s "$SUBMODULE_NAME")" ]; then
  echo "Submodule ${SUBMODULE_NAME} has not changed"
else
  >&2 echo "Submodule ${SUBMODULE_NAME} has uncommitted changes"
  git diff "$SUBMODULE_NAME" 1>&2
fi

