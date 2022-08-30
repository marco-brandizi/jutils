# Local customisations injected inside the workflow of build.sh
# See https://github.com/Rothamsted/knetminer-common for details. 
#
if [[ "$GIT_BRANCH" == 'master' ]]; then
	bash ./ci-build/mk-javadocs.sh
	
	echo -e "\n  Committing Javadocs\n"
	git add docs/javadocs
  git commit -a -m "Updating auto-generated files from CI $CI_SKIP_TAG"
fi

export NEEDS_PUSH=true # instructs build.sh to push the changes above
