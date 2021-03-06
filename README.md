[![Release](https://jitpack.io/v/masterwok/simple-torrent-android.svg)](https://jitpack.io/#masterwok/simple-torrent-android)

# simple-torrent-android
An Android torrent client library powered by [frostwire-jlibtorrent](https://github.com/frostwire/frostwire-jlibtorrent). It supports sequential and simultaneous downloads.


## Usage

A single torrent download session is represented as the ```TorrentSession``` class. An instance of this class can be used to start, stop, pause, and resume a torrent download. When creating a new session, a ```TorrentSessionOptions``` instance along with a torrent ```Uri``` are required constructor parameters. The ```TorrentSessionOptions.Builder``` class can be used to create session options. The ```TorrentSessionListener``` interface can be implemented and set on the session to receive ```TorrentSessionStatus``` updates tied to the lifecycle of the torrent. The ```TorrentSessionBufferState``` is one of the properties of the status and represents the current state of the torrent piece buffer.

For example, the following code snippet sequentially downloads the largest file of the provided torrent to the downloads directory:

```kotlin
// http, https, magnet, file, and content Uri types are all supported.
val torrentUrl = Uri.parse("http://www.frostclick.com/torrents/video/animation/Big_Buck_Bunny_1080p_surround_frostclick.com_frostwire.com.torrent")
val timeoutSeconds = 60

val torrentSessionOptions = TorrentSessionOptions
        .Builder(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))
        .onlyDownloadLargestFile(true)
        .anonymousMode(true)
        .stream(true)
        .build()

val torrentSession = TorrentSession(torrentUrl, torrentSessionOptions)

torrentSession.listener = object : TorrentSessionListener {
    // Omitted for brevity
}

torrentSession.start(context, timeoutSeconds) 

```

For a more detailed example, please see the sample application alongside this library (screenshot below).

## Configuration

Add this in your root build.gradle at the end of repositories:
```gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```
and add the following in the dependent module:

```gradle
dependencies {
    implementation 'com.github.masterwok:simple-torrent-android:0.0.4'
}
```
unless you're a fan of large APKs, you'll probably want to add the following to the build.gradle of your app so an APK is generated per ABI:

```gradle
android {
    ...
    splits {
        abi {
            enable true
            reset()
            include 'armeabi-v7a', 'arm64-v8a', 'x86', 'x86_64'
            universalApk false
        }
    }
}

// Map for the version code that gives each ABI a value.
ext.abiCodes = [
        'armeabi-v7a': 1,
        'arm64-v8a'  : 2,
        'x86'        : 3,
        'x86_64'     : 4
]

import com.android.build.OutputFile

android.applicationVariants.all { variant ->
    variant.outputs.each { output ->
        def baseAbiVersionCode = project.ext.abiCodes.get(output.getFilter(OutputFile.ABI))

        if (baseAbiVersionCode != null) {
            output.versionCodeOverride = baseAbiVersionCode * 10000000 + variant.versionCode
        }
    }
}
```

## Demo Screenshots

<img src="/app/screenshots/example.jpg?raw=true" height="600" title="Demo">
