#define MyAppName "Penjualan Rumah"
#define MyAppVersion "1.5"
#define MyAppPublisher "Syauqi"
#define MyAppJar "GUIP5.jar"

[Setup]
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}

DefaultDirName={autopf}\{#MyAppName}
DefaultGroupName={#MyAppName}

OutputDir=D:\Java\Pertemuan_8
OutputBaseFilename=INSTALLER_JAVA

Compression=lzma
SolidCompression=yes
WizardStyle=modern


[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Files]
Source: "D:\Java\Pertemuan_8\dist\GUIP5.jar"; DestDir: "{app}"; Flags: ignoreversion

; kalau ada folder lib buka komentar ini
; Source: "D:\Java\Pertemuan_8\dist\lib\*"; DestDir: "{app}\lib"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{autoprograms}\{#MyAppName}"; Filename: "javaw.exe"; Parameters: "-jar ""{app}\{#MyAppJar}"""; IconFilename: "{app}\icon.ico"

Name: "{autodesktop}\{#MyAppName}"; Filename: "javaw.exe"; Parameters: "-jar ""{app}\{#MyAppJar}"""; IconFilename: "{app}\icon.ico"

[Run]
Filename: "javaw.exe"; Parameters: "-jar ""{app}\{#MyAppJar}"""; Flags: nowait postinstall skipifsilent