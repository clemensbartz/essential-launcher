# Essential Launcher

[![Build Status](https://travis-ci.org/clemensbartz/essential-launcher.svg?branch=release%2Fv1.3)](https://travis-ci.org/clemensbartz/essential-launcher)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/1e17dc4e83d748a7bf35231ed7fa9528)](https://www.codacy.com/app/clemensbartz/essential-launcher?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=clemensbartz/essential-launcher&amp;utm_campaign=Badge_Grade)

## Overview

Essential Launcher is a small launcher for Android. It provides a minimum of functionality.

<a href="https://f-droid.org/packages/de.clemensbartz.android.launcher/" target="_blank">
<img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="80"/></a>
<a href="https://play.google.com/store/apps/details?id=de.clemensbartz.android.launcher" target="_blank">
<img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png" alt="Get it on Google Play" height="80"/></a>

## Features

- Small:
    - <30 KB APK size (without [Minify](http://developer.android.com/tools/help/proguard.html))
    - <150 KB application size on a device
- App drawer to launch, uninstall, and show information about that app.
- Home screen with ability to add a widget.
- Dock with four frequently used applications.
- Transparent background.
- LTR as well as RTL support.
- Accessibility enabled.

## Development

If you want to support development, please make sure:

- You use Android Studio >= 3.0.1
- You must not enable minify.
- You use SDK Version >= 27.
- You specify minimum SDK Version as 21.
- You use Java >= 8.
- The maximum size for the APK is 30 KB.
- You add your own `local.properties` file pointing to your android sdk: `sdk.dir=/opt/android-sdk-linux/`

## Not yet supported features

- Widgets are not automatically advanced.
- Filter in Drawer

## License

Copyright (C) 2017 Clemens Bartz

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
