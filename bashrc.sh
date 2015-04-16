# should be sourced by .bashrc

########## local properties ##########

PATH_TO_FF=~/git/hub/ff
PATH_TO_ANTLR4=/usr/local/lib/antlr-4.5-complete.jar
PATH_TO_ANDROID_SDK=~/git/pkg/android-sdk-linux

########## END local properties ##########

capitalize() {
	echo $(tr '[:lower:]' '[:upper:]' <<< ${1:0:1})${1:1}
}

lowercase() {
	echo $1 | tr '[:upper:]' '[:lower:]'
}

ff_build_grammar() {
	(cd $PATH_TO_FF && java -jar $PATH_TO_ANTLR4 Ff.g4)
}

# "$1" --> package name
# "$2" --> source java file
# "$3" --> destination
ff_move_with_package() {
	local dest="$3"/"$(basename "$2")"
	printf "package $1;\n" > "$dest" && cat "$2" >> "$dest"
}

# "$1" --> create location (e.g. ./Sample)
# "$2" --> package name (e.g. com.jj.my.awesome.app)
ff_create_android_project() {

	ff_build_grammar

	if [ $? != 0 ]; then
		echo "failed to build ff"
		return 1
	else
		echo "ff built successfully"
	fi

	if [ -e "$1" ]; then
		rm -rf "$1"
	fi

	$PATH_TO_ANDROID_SDK/tools/android create project \
		--target "android-22" \
		--path "$1" \
		--package "$2" \
		--activity MainActivity > /dev/null

	if [ $? != 0 ]; then
		echo "failed to create android project"
		return 1
	else
		echo "successfully created android project"
	fi

	ff_move_with_package \
		"$2" \
		$PATH_TO_FF/MainActivity.java \
		"$1"/src/"${2//\./\/}/"
}
