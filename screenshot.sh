#!/bin/bash

fname=$1
adb shell screencap -p /sdcard/screen.png
adb pull /sdcard/screen.png ${fname}
adb shell rm /sdcard/screen.png
