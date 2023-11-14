#!/bin/bash

params=$1

script_dir=$(dirname "$0")

appList=$(pm list packages -f "$params" | grep -o ':.*=')
IFS=$'\n'
for pn in $appList
do

  pn=${pn#:}
  pn=${pn%=}

##  installPath=$(pm path "$pn" | cut -d ':' -f 2)
  info=$("$script_dir/aapt-arm-pie" d badging "$pn" | grep -i -E "package:|sdkVersion|targetSdkVersion|application-label:'|application-label-zh:|application-label-zh-CN:")

  label=""
  labelZh=""
  labelZhCn=""
  packageInfo=""
  sdkVersion=""
  targetSdkVersion=""
  size=$(du -b "$pn")
  length=$(echo "$size" | awk '{print $1}')
  for i in $info; do
    if [[ "$i" == *"package: "*  ]]; then
      packageInfo=$i
    fi
    if [[ "$i" == *"sdkVersion:"* ]]; then
      sdkVersion=$i
    fi
    if [[ "$i" == *"targetSdkVersion:"* ]]; then
      targetSdkVersion=$i
    fi
    if [[ "$i" == *"application-label:'"* ]]; then
      label=$i
    fi
    if [[ "$i" == *"application-label-zh-CN:'"* ]]; then
      labelZhCn=$i
    fi
    if [[ "$i" == *"application-label-zh:'"* ]]; then
      labelZh=$i
    fi
  done
  zh=""
  if [ -z "$labelZhCn" ]; then
    zh=$labelZh
  else
    zh=$labelZhCn
  fi
  echo "$label<tb>$zh<tb>$pn<tb>$packageInfo<tb>$sdkVersion<tb>$targetSdkVersion<tb>$length"
done
