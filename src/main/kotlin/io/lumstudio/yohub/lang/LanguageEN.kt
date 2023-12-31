﻿package io.lumstudio.yohub.lang

/**
 * 百分号为格式化标识，不需要翻译
 */
object LanguageEN : LanguageBasic(
    appName = "YoHub Desktop",
    inPreparation = "Ready...",
    initAdbRuntime = "Initializing ADB runtime...",
    initPythonRuntime = "Initializing Python runtime...",
    initPayloadDumper = "Initializing Payload Dumper runtime....",
    initMagiskPatcher = "Initializing Magisk Patcher runtime...",
    initAdbDriver = "Initializing Android driver...",
    initAndroidToolkit = "Initializing Android toolkit...",
    finished = "Loading success!",
    themeColorLoadFailedTitle = "Theme color loading failed!",
    themeColorLoadFailedMessage = "%s, The theme color has been set as the default.",
    searchFunctions = "Search functions",
    tooltipTextDevice = "Devices",
    unlinkDevice = "Unconnected device",
    refreshDeviceList = "Click refresh device list",
    checkIndexDevice = "Indexing...",
    deviceType = "Device type：%s",
    deviceTypeUnAuthorization = "%s（unauthorized）",
    unknownDevice = "Unknown device",
    unknown = "Unknown",
    driverState = "Driving state：%s",
    normal = "Normal",
    exception = "Exception (click Repair)",
    notChooseDevice = "No devices are selected",
    linkedDevice = "Connected: %s",
    tips = "Tips",
    defined = "Confirm",
    cancel = "Cancel",
    dialogRebootDevice = "Are you sure you want to restart [%s]?",
    rebootDevice = "Restart",
    dialogShutdownDevice = "Are you sure you want to turn off the device [%s]?",
    shutdownDevice = "Shutdown",
    dialogRebootBootloaderDevice = "Are you sure you want to restart [%s] to Bootloader mode?",
    rebootBootloaderDevice = "Restart to Bootloader mode",
    dialogRebootRecoveryDevide = "Are you sure you want to restart [%s] to Recovery mode?",
    rebootRecoveryDevide = "Restart to Recovery mode",
    labelHome = "Home",
    labelPayload = "Payload Unzip",
    titlePayload = "Extract the image file",
    subtitlePayload = "Click [Payload Unzip] on the right.",
    labelMagiskArea = "Magisk Area",
    titleMagiskArea = "Magisk related functions",
    subtitleMagiskArea = "Click [Magisk Area] on the right.",
    labelMagiskPatcher = "Magisk Patcher",
    titleMagiskPatcher = "Patch Boot image（Root）",
    subtitleMagiskPatcher = "Click [Magisk Patcher] on the right.",
    labelMagiskRepository = "Magisk Repository",
    labelAdbArea = "ADB Area",
    titleAdbArea = "Customize device settings",
    subtitleAdbArea = "Click [ADB Area] on the right.",
    labelAdbInstaller = "App Installer",
    labelAdbPicker = "App Manager",
    labelAdbActiveArea = "One-click activation area",
    labelSettings = "Settings",
    labelFlashImage = "Image Flasher",
    titleFlashImage = "Flash image file for device",
    subtitleFlashImage = "Click [Image Flasher] on the right.",
    labelUnlink = "Fastboot device is not connected.",
    titleUnlink = "Please connect Fastboot device.",
    labelFlashImageLinked = "Image Flasher",
    labelTheme = "Theme Setting",
    labelVersion = "Version",
    labelOpenSourceLicense = "Open Source License",
    noticeTitleActiveSuccess = "Activation succeeded!",
    noticeMessageActiveSuccess = "[Shizuku] ADB activated successfully! Exit code: 0",
    noticeTitleActiveFail = "Activation exception!",
    activeShizuku = "Activate Shizuku",
    noticeTitleSuccessBlackScope = "[Brevent] ADB activated successfully! Start the black domain APP again to use.",
    activeBlackScope = "Activate Brevent",
    appList = "Apps",
    findApps = "Number of apps: %d",
    inputSaveApkPath = "Enter the path to save the installation package",
    chooseFile = "Select file",
    chooseDir = "Select folder",
    openFileManager = "Open in Explorer",
    searchApps = "Search Apps (Package Name)",
    all = "All",
    system = "System",
    user = "User",
    pickApp = "Picking [%s]",
    noticePickAppSuccess = "Picking succeeded!",
    noticePickAppFail = "Picking failed!",
    noticeMessagePickAppFail = "Please check that the path to save the installation package is correct!",
    startInstall = "Install【%s】",
    notChooseApk = "No APK file selected",
    inputApkPath = "Enter the path of APK file (APK/APEX is supported)",
    chooseFail = "Selection failed",
    chooseFailMessage = "Unsupported file type: %s",
    apkInstalling = "Please wait while the application is installed...",
    installSuccess = "Installation succeeded.!",
    installSuccessMessage = "Installed [%s] in device [%s].",
    installFail = "Installation failed!",
    pleaseLinkDevice = "Please connect the device",
    pleaseChooseDevice = "Please select a device",
    pleaseAuthorizeDevice = "Please reconnect this device and grant permissions on it",
    pleaseLinkAdbDevice = "Please connect ADB device",
    linkedAdbDevice = "Connected device: %s",
    loading = "Loading...",
    deviceInfo = "Detail of Devices",
    adbAreaFunctions = "ADB",
    insufficientPermissions = "Insufficient permissions!",
    adbPhoneInfo = "Device:\n%s\nSystem version:\nAndroid %s（%s）\nSOC:\n%s\nCores: %d cores\nBattery capacity: %s\nBattery temperature: %s\n%sGPU：%s",
    externalStorageInfo = "Total: %s GB\nUsed: %s GB\nFree: %s GB",
    externalStorageSpace = "External\nStorage",
    memorySpace = "Memory    %.2f%s(%dGB)",
    swapSpace = "Swap    %.2f%s(%dGB)",
    coreLoad = "Load: %.2f%s",
    flashImage = "Flash Image",
    flashConfirm = "Confirm writing",
    flashMessage = "Do you want to perform the flash task?\n\nFile selected: %s\nPartition selected: %s",
    flashing = "flashing...",
    flashSuccess = "Flashed succeeded!",
    flashSuccessMessage = "Total elapsed time: %s seconds",
    flashFail = "Flashed failed!",
    inputImagePath = "Enter image file path",
    choosePartition = "Select partition",
    partitionSelected = "Partition selected: %s",
    aBSlotFilter = "Filter A/B partition",
    textChoosePartition = "Select partition",
    startPatcherImage = "Patching Image",
    notChooseBoot = "Boot file not selected.",
    inputBootPath = "Enter the path of image file (boot/init_boot is supported)",
    bootOutputPath = "Boot file output path",
    bootPatching = "Please wait while Boot is being patched...",
    patchSuccess = "Patched succeeded!",
    patchSuccessMessage = "The patched image file [%s] has been stored in the %s.",
    patchFail = "Patched failed!",
    searchMagiskVersion = "Search Magisk",
    magiskList = "Magisk Release",
    fastLoading = "loading...",
    magiskVersionSubtitle = "Version: %s  Size: %sMB  Downloads: %d",
    downloadApk = "Download",
    lineOne = "Line 1",
    lineTwo = "Line 2",
    loadFailAndRetry = "Loading failed. Click Retry.",
    imageList = "Images",
    notChoosePayloadFile = "No Payload.bin file is selected.",
    downloadMIUIRom = "Download MIUI",
    inputPayloadPath = "Enter the path of the payload.bin file",
    imageOutputPath = "Image file save path",
    searchImage = "Search Images",
    refreshList = "Refresh",
    findImageCount = "Images: %d",
    pickImage = "Picking %s.img",
    imagePickedSaveAt = "The file has been saved in %s.",
    appCopyright = "Copyright @ 2023 YouTanYun All Rights Reserved",
    darkMode = "Dark Mode",
    themeColor = "Theme Color",
    generateThemeFile = "Generate theme file",
    uninstallFail = "uninstall failed!",
    uninstallFailMessage = "Cannot uninstall theme that is currently in use",
    uninstallThemeTip = "Do you want to uninstall theme [%s]\nFile name: %s?",
    generateTheme = "Make Theme",
    analysisFail = "Parsing failed!",
    pleaseInputColorFilePath = "Please enter Color file path",
    themeAnalysisFail = "Theme parsing failed!",
    isNotEffectiveColorFile = "Is not a valid Color file",
    themeCreateFail = "Theme creation failed!",
    themeNameCannotEmpty = "Theme name cannot be empty!",
    themeGenerateSuccess = "Theme generated successfully!",
    themeGenerateSuccessMessage = "Your theme [%s] has been saved in the %s.",
    themeGenerateError = "Theme parsing error!",
    themeGenerateFail = "Theme generation failed!",
    targetFileIsNotExists = "The target file [%s] does not exist",
    help = "Help",
    iKnown = "OK",
    getColorFile = "Get Color file",
    generateColorThemeMessage = "The theme color matching architecture of this software is designed according to Material Design 3(MD3) design specification. In order to facilitate theme designers to create theme configuration files, this software will directly parse the \"Color.kt\" file generated by Material Theme Builder and automatically convert it into theme configuration files.\n\nGet Color.kt: Click the get button below, then design your theme color scheme in the website, and finally export it as \"Jetpack Compose\". Finally, extract the downloaded file into the directory and use this software to import the Color.kt file.\n\nTip: It's just convenient to use the \"Material Theme Builder\" plugin in [Figma] to design color matching~",
    themeName = "*Theme Name",
    colorFilePath = "*Enter the file path of \"Color.kt\"",
    installTheme = "Install Theme",
    openSourceLicenseUrl = "Source Code",
    gotoUrl = "View on GitHub",
    themeAnalysisError = "Theme parsing error!",
    themeAnalysisMessage = "Failed to parse theme file [%s]: %s",
    installThemeSuccess = "Successfully installed theme [%s]",
    themeAnalysisFailMessage = "Failed to parse theme file [%s]: %s",
    uninstallSuccess = "Uninstallation succeeded！",
    uninstallSuccessMessage = "Theme file [%s] has been uninstalled",
    themeFileIsNotExists = "Theme file [%s] may not exist",
    darkModeSystem = "System",
    darkModeLight = "Light",
    darkModeDark = "Dark",
    language = "Language",
    languageSetting = "Global Language",
    defaultLanguage = "System",
    appInfoFormat = "Package: %s   Size：%s\nVersion：%s   TargetSdk：%s",
    labelAdvancedFunction = "Advanced Functions",
    rootFunction = "(This feature requires Root privileges)",
    labelImageBackup = "Partition Backup",
    checkRootTip = "In detecting Root permission, please allow the [Shell] application to obtain Root permission.",
    notRootAndRetry = "Root permission was not obtained. Click Retry.",
    labelAdvancedSetting = "Advanced Settings",
    adminCode = "Administrator Permission Command",
    adminCodeSubtitle = "Some devices may need to change the SU command in order to gain Root privileges",
    imageBackupPath = "Storage path of partition backup file",
    partitionList = "Partitions",
    searchPartition = "Search Partition",
    selectAll = "Select All",
    selectNone = "Select None",
    findPartitions = "%d Partitions, %d Selected",
    textBackupPartitions = "Start Backup",
    backupFail = "Backup failed!",
    noSelectPartitionMessage = "At least one partition needs to be selected for backup.",
    backupTaskTitle = "In partition backup",
    backupTaskMessage = "Please don't disconnect or turn off the mobile phone while the partition backup task is in progress, otherwise the data may be damaged!",
    backupImageCancel = "Backup Task Canceled",
    backupImageCancelMessage = "The backup task has been cancelled. Out of %d tasks, %d finished, time: %s",
    tryCancel = "Canceling...",
    backupFolderIsNotExists = "Please enter a correct backup path!",
    backupFinished = "Backup completed!",
    backupFinishedMessage = "The backup task has been completed! %d succeeded, %d failed, a total of %d tasks, time: %s",
    confirmBackupImage = "Are you sure you want to perform the backup partition task? Please don't disconnect or turn off the device! The process may last for a long time, ranging from 5 to 30 minutes, depending on the performance of the device and the connection speed between the mobile phone and the computer.\n\nTasks: %d tasks in total. Click [Confirm] to continue.",
    backupAllImage = "Back up the complete font",
    hasNewVersion = "New Version!",
    gotoDownload = "Download",
    updateVersionText = "Version: %s\nUpdate: %s\nSize: %s   Download: %d",
    deviceSlot = "Device Slot: %s",
    appDisabled = " (Disabled)",
    textPickup = "Pickup",
    textDisable = "Disable",
    textEnable = "Enable",
    textUninstall = "Uninstall",
    noticeAppEnabled = "Successfully enabled!",
    noticeAppEnabledMessage = "[%s] has been successfully enabled",
    noticeAppEnabledFail = "Enabled failed!",
    noticeAppDisabled = "Successfully disabled!",
    noticeAppDisabledMessage = "[%s] has been successfully disabled",
    noticeAppDisabledFail = "Disabled failed!",
    noticeAppUninstall = "Uninstalled succeeded!",
    noticeAppUninstallMessage = "[%s] has been uninstalled from device",
    noticeAppUninstallFail = "Uninstalled failed!",
    dialogUninstallApp = "Uninstall the application [%s] from [%s]? \n\nClick the confirm button to continue.",
    magiskVersion = "Select Magisk Patcher Version",
    labelFastRoot = "",
    titleFastRoot = "",
    subtitleFastRoot = "",
    appVersion = "Version：%s",
    checkVerion = "Check New Feature",
    softVersion = "App Version",
    isLatest = "Latest version",
    appHasError = "An Error Occurred In Application",
    openNav = "Open Nav",
    collapseNav = "Collapse Nav",
    searchFuns = "Search Functions",

)
