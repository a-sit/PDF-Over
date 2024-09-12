#!/bin/bash
set -o errexit   # abort on nonzero exitstatus
set -o nounset   # abort on unbound variable
set -o pipefail  # don't hide errors within pipes

pwd
cd ./pdf-over-build
ls


mkdir debian
touch debian/changelog

prevtag="pdf-over-4.0.0"
pkgname="pdf-over-nightly"
git tag -l  | sort -V | while read tag; do
    (echo -e "$pkgname (${tag#pdf-over-}); urgency=medium \n"; git log --pretty=format:'  * %s' $prevtag..$tag; git log --pretty='format:%n%n -- %aN <%aE>  %aD%n%n' $tag^..$tag) | cat - debian/changelog | sponge debian/changelog
        prevtag=$tag
done

tag=`git tag -l v* | sort -V | tail -1`
[ `git log --exit-code $tag..HEAD | wc -l` -ne 0 ] && git-dch -s $tag -S --no-multimaint --nmu --ignore-branch --snapshot-number="'{:%Y%m%d%H%M%S}'.format(__import__('datetime').datetime.fromtimestamp(`git log -1 --pretty=format:%at`))"

#sed -i 's/UNRELEASED/unstable/' debian/changelog

echo "Generating changelog done!"
echo " generated `wc -l debian/changelog` lines."

git tag
git log
