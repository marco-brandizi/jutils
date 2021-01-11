set -e
ci_skip_tag='[ci skip]'

if [[ `git log -1 --pretty=format:"%s"` =~ "$ci_skip_tag" ]]; then
	echo -e "\n$ci_skip_tag prefix, ignoring this commit\n"
	exit
fi

export GIT_BRANCH=`git branch --show-current`

mvn deploy --settings maven-settings.xml  --no-transfer-progress --batch-mode

# PRs are checked out in detach mode, so they haven't any branch, so this catches them too
if [[ "$GIT_BRANCH" != 'master' ]]; then
	echo -e "\nThis isn't main/master, skipping Javadoc\n"
	exit
fi 
	
./mk-javadocs.sh
git config --global user.email "marco.brandizi@gmail.com"
git config --global user.name "marco-brandizi"
git remote set-url origin https://marco-brandizi:$GITHUB_TOKEN@github.com/marco-brandizi/jutils 
git commit -a -m "Updating auto-generated files from CI $ci_skip_tag" --author='[CI job]'
#git push origin HEAD:"$GIT_BRANCH"
git push
