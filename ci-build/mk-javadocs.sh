# Make the javadocs of each module under the main docs directory.
# This is usually run by Travis during builds

echo -e "\n\n\t---- Making Javadocs for jutils\n"

set -e 
set -o pipefail

export JAVADOC_REL_PATH=docs/javadocs
cd "`dirname $0`/.."

rm -Rf "$JAVADOC_REL_PATH/jutils"
mvn javadoc:javadoc -DreportOutputDirectory="$JAVADOC_REL_PATH" --no-transfer-progress --batch-mode

echo -e "\n\n\tThe End\n"
