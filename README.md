# MobyDroid

MobyDroid is a simple and cross-platform Android Devices Manager.
It runs on any operating system with Java support (Mac OS X, Windows, Linux, BSD, Solaris...).

THIS IS JUST A BETA RELEASE !

### Prerequisites ###
- Java Runtime Environment (JRE) 1.8 or later
- ADB

### Usage ###
Simply extract and run the jar file:
```
java -jar "mobydroid.jar"
```

### Motives ###
I have been running linux from while now.
and what i noticed is there is no GUI Android Devices Manager until now (QtADB didn't work for me).
So basicly I kept using ADB command line and kept waiting for someone to make something or some alternatives.
So some day a friend of mine kept complaining about the same problem.
So i decided to make a simple Apk installer with a graphical user interface.
And something lead to another.
And i wanted to share it for any one haveing the same problem.

So here it is !


## Download ##

| Type | Size | Download Link |
| - | - | - |
| Standalone | 2.29 MB | [MobyDroid_v0.1.zip](https://github.com/ibilux/MobyDroid/releases/download/v0.1/MobyDroid_v0.1.zip) |
| Jar Only | 0.22 MB | [MobyDroid_v0.1_jar.zip](https://github.com/ibilux/MobyDroid/releases/download/v0.1/MobyDroid_v0.1_jar.zip) |


## BETA ##

THIS IS JUST A BETA RELEASE !
So you should expect a ton of bugs or crash issues.
Supporting _MobyDroid with issue reports, and great ideas.
if you encounter an issue or have agreat ideas please file an issue on [issues](https://github.com/ibilux/MobyDroid/issues).


## How to Install ADB on Windows, macOS, and Linux ##

A Great article on XDA [see here](https://www.xda-developers.com/install-adb-windows-macos-linux/)


## Troubleshooting
If you cannot connect to your device check the following:
- Your adb server is running by issuing `adb start-server`
- You can see the device using adb `adb devices`

If you see the device in `adb` but not in `MobyDroid` please file an issue on [issues](https://github.com/ibilux/MobyDroid/issues).

## ADB Protocol Description ##

The Android Debug Bridge (ADB) is a client-server architecture used to communicate with Android devices (install APKs, debug apps, etc).

An overview of the protocol can be found here: [Overview](https://android.googlesource.com/platform/system/adb/+/master/OVERVIEW.TXT)

A list of the available commands that a ADB Server may accept can be found here:
[Services](https://android.googlesource.com/platform/system/adb/+/master/SERVICES.TXT)

The description for the protocol for transfering files can be found here: [SYNC.TXT](https://android.googlesource.com/platform/system/adb/+/master/SYNC.TXT).


## Contributing ##
This project would not be where it is, if it where not for the helpful [contributors](https://github.com/ibilux/MobyDroid/graphs/contributors).
Supporting _MobyDroid with issue reports, and great ideas.
The original author and all users of this project are very greatful for your contribution to this Open Source Project.


## Authors ##
Bilux <i.bilux@gmail.com>

See [contributors](https://github.com/ibilux/MobyDroid/graphs/contributors) for a full list.


## License ##
This project is currently released under the Apache License Version 2.0, see [LICENSE.md](LICENSE.md) for more information.

