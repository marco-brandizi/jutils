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
	
	[[ "$CI_GIT_BRANCH" == 'master' ]] || return 0

	bash ./ci-build/mk-javadocs.sh
	
	printf "== Committing Javadocs\n"
	git add docs/apidocs
  git commit -a -m "Updating auto-generated files from CI $CI_SKIP_TAG"

  export CI_NEEDS_PUSH=true # Instructs the git update stage that we have stuff to push
}



function install_and_import 
{
	url_base="$1"

	printf "\n== Downloading from URL base '%s'\n\n" "$url_base"

	# Relative to the <git root>/ci-build-v2
	for file_local_path in "_common.sh" "java-maven/_common.sh" "java-maven/maven-settings.xml"
	do
		file_local_path="ci-build-v2/$file_local_path"
		[[ ! -e "$file_local_path" ]] || continue;
				
		url="$url_base/${file_local_path}"
		file_path="$(realpath "${file_local_path}")"		
		dir_path="$(dirname "${file_path}")"
		
		printf "= Downloading '%s' to '%s'" "$url" "${file_path}\n"

		mkdir -p "${dir_path}"
		curl --fail-with-body "$url" -o "${file_path}"
	done

	# Eventually, these should be here.
	. ./ci-build-v2/java-maven/_common.sh	

	# WARNING: the best way to override functions defined in these imported files is 
	# doing it in your own definition file and then importing it after the original ones.
	# This is not necessary if you only have stage_xxx_local() functions (or only your own new
	# functions) 
	# . ./ci-build-v2/java-maven/_common-local.sh
}

install_and_import "https://raw.githubusercontent.com/Rothamsted/knetminer-common/refs/heads/ci-build-v2"
main
