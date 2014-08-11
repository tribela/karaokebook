#!/bin/bash

screenshot() {
    local fname=$1
    adb shell screencap -p /sdcard/screen.png
    adb pull /sdcard/screen.png ${fname}
    adb shell rm /sdcard/screen.png
}

PACKAGE='kai.search.karaokebook'
ACTIVIRY='.Main'

SCREENSHOT_DIR='screenshots'
APPEND_STR=$1

adb shell am start -n ${PACKAGE}/${ACTIVIRY} -e fragment_position 0
sleep 1
screenshot ${SCREENSHOT_DIR}/main${APPEND_STR}.png

adb shell am start -n ${PACKAGE}/${ACTIVIRY} -e fragment_position 1
sleep 1
screenshot ${SCREENSHOT_DIR}/favourites${APPEND_STR}.png

adb shell am start -n ${PACKAGE}/${ACTIVIRY} -e fragment_position 2
sleep 1
screenshot ${SCREENSHOT_DIR}/setting${APPEND_STR}.png
