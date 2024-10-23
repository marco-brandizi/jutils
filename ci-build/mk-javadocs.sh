# Make the javadocs of each module under the main docs directory.
# This is usually run during CI builds

echo -e "\n\n\t---- Making Javadocs for jutils\n"

set -e 
set -o pipefail

export JAVADOC_REL_PATH=docs/apidocs
cd "`dirname $0`/.."

rm -Rf "$JAVADOC_REL_PATH/jutils"
#mvn javadoc:javadoc -DreportOutputDirectory="$JAVADOC_REL_PATH" --no-transfer-progress --batch-mode

mvn javadoc:javadoc --no-transfer-progress --batch-mode

echo -e "\n\n\tThe End\n"
