# should be sourced by .bashrc

########## local properties ##########

PATH_TO_FF=${PATH_TO_FF:-~/git/hub/ff}
PATH_TO_ANTLR4=${PATH_TO_ANTLR4:-/usr/local/lib/antlr-4.5-complete.jar}
PATH_TO_ANDROID_SDK=${PATH_TO_ANDROID_SDK:-~/git/pkg/android-sdk-linux}

########## END local properties ##########

capitalize() {
	echo $(tr '[:lower:]' '[:upper:]' <<< ${1:0:1})${1:1}
}

lowercase() {
	echo $1 | tr '[:upper:]' '[:lower:]'
}

ff_build_grammar() {
	(cd "$PATH_TO_FF" && java -jar "$PATH_TO_ANTLR4" Ff.g4)
}

# "$1" --> package name
# "$2" --> source java file
# "$3" --> destination
ff_copy_with_package() {
	local dest="$3"/"$(basename "$2")"
	printf "package $1;\n" > "$dest" && cat "$2" >> "$dest"
}

# "$1" --> create location (e.g. ./Sample)
# "$2" --> package name (e.g. com.aff.my.awesome.app)
# "$3" --> path to ff file
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

	"$PATH_TO_ANDROID_SDK"/tools/android create project \
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

	cp "$PATH_TO_ANTLR4" "$1"/libs/

	for filename in "$PATH_TO_FF"/{FfRuntime,FfCompiler,MainActivity,MainService,AndroidWidgetPlugin,AndroidAudioPlugin,FfLexer,FfBaseListener,FfListener,FfParser}.java; do

		echo $filename

		ff_copy_with_package "$2" \
			"$filename" \
			"$(echo "$1"/src/"${2//\.//}" | sed '#.#/#g')"/

	done

	printf "package $2;\n" > "$(echo "$1"/src/"${2//\.//}" | sed '#.#/#g')"/Program.java
	cat "$3" | python "$PATH_TO_FF"/toProgram.py >> "$(echo "$1"/src/"${2//\.//}" | sed '#.#/#g')"/Program.java

	sed -i 's$</activity>$</activity><service android:name="MainService" />$' "$1"/AndroidManifest.xml
	sed -i 's$</application>$</application><uses-permission android:name="android.permission.INTERNET"/>$' "$1"/AndroidManifest.xml

}

# "$1" --> package name (e.g. com.my.awesome.app)
# "$2" --> ff file (e.g. MyProgram.aff)
ff_android() {

	if [ $# != 2 ]; then
		echo "Usage: $0 <package name> <path to ff file>"
		return 1
	fi

	local loc="$(basename "$2" .aff)"

	ff_create_android_project "$loc" "$1" "$2" && \
	(cd "$loc" && ant debug && ant installd)
}

logcat() {
	"$PATH_TO_ANDROID_SDK"/platform-tools/adb logcat
}
