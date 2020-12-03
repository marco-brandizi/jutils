set -e
ci_skip_tag='[ci skip]'

if [[ `git log -1 --pretty=format:"%s"` =~ "$ci_skip_tag" ]]; then
	echo -e "\n[ci skip] prefix, ignoring this commit\n"
	exit
fi

mvn deploy --settings maven-settings.xml  --no-transfer-progress --batch-mode

if [[ ! -z "$CI_PULL_REQUEST" ]] || [[ "$CI_BRANCH" != 'master' ]]; then
	echo "PR: '$CI_PULL_REQUEST', BRANCH: '$CI_BRANCH'"
	echo -e "\nThis isn't main-repo/master, skipping Javadoc\n"
	exit
fi 
	
./mk-javadocs.sh
git remote set-url origin https://marco-brandizi:$GITHUB_TOKEN@github.com/marco-brandizi/jutils 
git commit -a -m "Updating auto-generated files from CI $ci_skip_tag"
git push origin HEAD:"$TRAVIS_BRANCH"
