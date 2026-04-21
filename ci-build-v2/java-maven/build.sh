#!/usr/bin/env bash
set -e


function stage_build_local
{
	stage_build
	
	# Javadocs only for the main trunk
	#
	[[ "$CI_GIT_BRANCH" == 'master' ]] || return 0

	bash ./ci-build-v2/java-maven/mk-javadocs.sh
	
	printf "== Committing Javadocs\n"
	git add docs/apidocs
  git commit -a -m "docs: update CI-generated javadoc files $CI_SKIP_TAG"

  export CI_NEEDS_PUSH=true # Instructs the git update stage that we have stuff to push
}

printf "== Installing ci-build scripts and then running the build\n"

# TODO: DO USE a version tag in place of main, DO MAKE your builds stable and predictable
ci_build_url_base="https://raw.githubusercontent.com/KnetMiner/knetminer-ci/refs/tags/1.0"
script_url="$ci_build_url_base/ci-build-v2/install.sh"
. <(curl --fail-with-body -o - "$script_url") "$ci_build_url_base" java-maven

main
