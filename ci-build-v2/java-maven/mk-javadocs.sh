# Make the javadocs of each module under the main docs directory.
# This is usually run during CI builds

set -e 
set -o pipefail

printf "\n\n=== Making Javadocs for jutils\n"

export JAVADOC_REL_PATH=docs/apidocs
cd "$(dirname $0)/.."
rm -Rf "$JAVADOC_REL_PATH/jutils"
mvn javadoc:javadoc --no-transfer-progress --batch-mode

printf "\n=== Javadoc generated\n"
