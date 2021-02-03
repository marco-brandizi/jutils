# Local customisations injected inside the workflow of build.sh
# See https://github.com/Rothamsted/knetminer-common for details  
#
./make-javadoc.sh
git commit -a -m "Updating auto-generated files from CI $CI_SKIP_TAG"
