#!/usr/bin/env bash
set -e

function stage_build_setup_local
{
	if [[ ! -z "$CI_SECRETS_GIST_TOKEN" ]]; then
		printf "== Downloading Project Secrets from gist location\n"
		gist_url="https://gist.githubusercontent.com/marco-brandizi/$CI_SECRETS_GIST_TOKEN/raw/github-brandizi-secrets.sh"
		source <(curl --fail-with-body -o - "$gist_url")
	fi
	
	stage_build_setup
}


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
ci_build_url_base="https://raw.githubusercontent.com/KnetMiner/knetminer-ci/refs/heads/ci-build-v2"
script_url="$ci_build_url_base/ci-build-v2/install.sh"
. <(curl --fail-with-body -o - "$script_url") "$ci_build_url_base" java-maven

main
