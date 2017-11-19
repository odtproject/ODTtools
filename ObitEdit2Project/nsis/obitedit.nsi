# some definitions to make it easier to set up the uninstall settings
# in Add/Remove Programs
!define APPNAME "${APP_NAME}"
!define COMPANYNAME "ODT"
# !define DESCRIPTION "Build or version info"

# These define the version number and must be integers
!define VERSIONMAJOR ${VERSIONMAJ}
!define VERSIONMINOR ${VERSIONMIN}
!define VERSIONBUILD ${VERSIONBLD}
!define VERSIONSTRING "${APPNAME}${VERSIONMAJOR}${VERSIONMINOR}"
!define VERSIONDOTTED "${VERSIONMAJOR}.${VERSIONMINOR}.${VERSIONBUILD}"

# This is the size (in kB) of all the files copied into "Program Files"
!define INSTALLSIZE 510

# This will be in the installer/uninstaller's title bar
Name "${COMPANYNAME} - ${APPNAME}"

# define the icon and name of the installer file
Icon "${FOLDER}\images\odt_logo.ico"
Outfile "${VERSIONSTRING}ins.exe"

# include definitions for macros
!include LogicLib.nsh

# require administrator rights
RequestExecutionLevel admin
VIProductVersion "${VERSIONDOTTED}"


# define the directory to install to, if x86 is defined, set explicitly to that
# there's bug in the Program Files decoding that messes up the icon setting
# InstallDirRegKey HKEY_LOCAL_MACHINE "SOFTWARE\Microsoft\Windows\CurrentVersion" "ProgramFiles (x86)"
InstallDir "$PROGRAMFILES\${COMPANYNAME}\${APPNAME}"

Page directory
Page instfiles
UninstPage uninstConfirm
UninstPage instfiles

# use this macro to verify that the user has
# administrator rights
!macro VerifyUserIsAdmin
UserInfo::GetAccountType
pop $0
${If} $0 != "admin" ;Require admin rights on NT4+
        messageBox mb_iconstop "Administrator rights required!"
        setErrorLevel 740 ;ERROR_ELEVATION_REQUIRED
        quit
${EndIf}
!macroend

# call the macro on init of the installer
function .onInit
	setShellVarContext all
	!insertmacro VerifyUserIsAdmin
functionEnd


Section "" # (default section)
 
# Set the output path for files
SetOutPath "$INSTDIR"
 
# define what to install
File "${FOLDER}\build\${EXE_NAME}"
File "${FOLDER}\images\odt_logo.ico"

# registry keys that Windows needs
WriteRegStr HKLM "SOFTWARE\${COMPANYNAME}\${APPNAME}" "" "$INSTDIR"

WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "DisplayName" "${COMPANYNAME} - ${APPNAME}"
WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "UninstallString" "$\"$INSTDIR\un${APPNAME}.exe$\""
WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "InstallLocation" "$\"$INSTDIR$\""
WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "DisplayIcon" "$\"$INSTDIR\odt_logo.ico$\""
WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "Publisher" "Obituary Daily Times"

WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "DisplayVersion" "${VERSIONMAJOR}.${VERSIONMINOR}.${VERSIONBUILD}"
WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "VersionMajor" ${VERSIONMAJOR}
WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "VersionMinor" ${VERSIONMINOR}

# There is no option for modifying or repairing the install
WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "NoModify" 1
WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "NoRepair" 1

# Set the INSTALLSIZE constant (!defined at the top of this script) so Add/Remove Programs can accurately report the size
WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "EstimatedSize" ${INSTALLSIZE}


# under the start/programs menu, put ObitEdit, and under there, ObitEdit and
# Uninstall ObitEdit
CreateDirectory "$SMPROGRAMS\${COMPANYNAME}"

#ReadRegStr $0 HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion" "ProgramFiles (x86)"
CreateShortCut "$SMPROGRAMS\${COMPANYNAME}\${APPNAME}.lnk" "$INSTDIR\${APPNAME}.exe" "" "$INSTDIR\odt_logo.ico"
CreateShortCut "$SMPROGRAMS\${COMPANYNAME}\Uninstall ${APPNAME}.lnk" "$INSTDIR\un${APPNAME}.exe"

# create desktop shortcut
SetOutPath "$DESKTOP"
CreateShortCut "$DESKTOP\${APPNAME}.lnk" "$INSTDIR\${APPNAME}.exe" "" "$INSTDIR\odt_logo.ico"

# write out the uninstaller
WriteUninstaller "$INSTDIR\un${APPNAME}.exe"
SectionEnd

#Install the other user files
Section "UserSetup"

# create the ODT folder and the Check folder
var /GLOBAL ODTDIR
StrCpy $ODTDIR "$PROFILE\ODT"
CreateDirectory "$ODTDIR"
CreateDirectory "$ODTDIR\Check"

# create the lib folder
var /GLOBAL LIBDIR
StrCpy $LIBDIR "$ODTDIR\lib"
SetOutPath $LIBDIR

# copy the info files to the lib directory
File ${FOLDER}\info\*

SectionEnd # end UserSetup

# begin uninstall settings/section
UninstallText "This will uninstall ObitEdit2 from your system"


Section Uninstall

# have to set this for the uninstall or the shortcuts don't get removed
setShellVarContext all

# delete desktop shortcut
Delete "$DESKTOP\${APPNAME}.lnk"

# delete the start menu shortcuts and folder
Delete "$SMPROGRAMS\${COMPANYNAME}\${APPNAME}.lnk"
Delete "$SMPROGRAMS\${COMPANYNAME}\Uninstall ${APPNAME}.lnk"
RMDir  "$SMPROGRAMS\${COMPANYNAME}\${APPNAME}"

# Try to remove the Start Menu top folder - this will only happen if it is empty
RMDir "$SMPROGRAMS\${COMPANYNAME}"

# delete the program itself and the logo icon
Delete "$INSTDIR\${APPNAME}.exe"
Delete "$INSTDIR\odt_logo.ico"

# delete registry keys
DeleteRegKey HKLM "SOFTWARE\${COMPANYNAME}\${APPNAME}"
DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}"

# Delete the uninstaller itself
Delete "$INSTDIR\un${APPNAME}.exe"

# this will only work if it's empty
RMDir  "$INSTDIR"

SectionEnd # end of uninstall section