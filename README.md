List of dependencies:

	* android-sdk (download from website)
		* android-22 build-tools (./android and follow gui)
		* android-22 android api (./android and follow gui)
	* ant (sudo apt-get install ant)
	* antlr4 (procure antlr-4.5-complete.jar)

Dependent vars

	* PATH_TO_ANTLR4 (where is antlr-4.5-complete.jar?)
	* PATH_TO_ANDROID_SDK (where is android-sdk-linux)
	* PATH_TO_FF (where did you git clone ff?)

Weird problems...

[Might need to install 32-bit libraries since aapt is 32-bit](http://stackoverflow.com/questions/18928164/android-studio-cannot-find-aapt/18930424#18930424)

	* sudo apt-get install lib32stdc++6
	* sudo apt-get install lib32z1

Seems to have been the case for 14.04, but not for 14.10.


------------

non-android version.

javac FfTerminalRuntime.java && \
echo "print('hello world'); screen.setChar('a', 0, 10);" \
| java FfTerminalRuntime
