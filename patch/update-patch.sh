# Script for updating the Minecraft patch.
# Requires a repository with the Minecraft source code in the "mc" folder
# next to "src".
# Branches in that repo must be set up as follows:
# master: vanilla Minecraft code, as generated by the MCP.
# modded: modified Minecraft code, as generated by the apply-patch script.

if [ -d ../mc ]; then
	cd ../mc

	git checkout master
	git checkout -b tmp
	git merge --squash modded
	git commit -a -m "mod"
	git format-patch master --ignore-space-change
	git checkout modded
	git branch -D tmp

	mv 0001-mod.patch ../patch/minecraft.patch
else
	echo "MCP-generated Minecraft source not found!"
	echo "Put it in a folder called \"mc\" next to \"src\"."
fi
