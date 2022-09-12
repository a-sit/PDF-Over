==== RELEASE PREPARATION (versioning)
(this creates a release tag which has versioning fields set appropriately, tags it, and pushes it to gitlab)
  -> switch to your pdf-over clone
  -> run mvn install to verify that it builds
    -> you can skip this step if you are feeling brave
  -> if using WSL, ensure that your line endings are correct
    -> git add --renormalize .
  -> to actually prep a release, run mvn release:prepare -Dresume=false
  -> this will prompt in the following order:
    -> current release version (x.y.z)
    -> current release tag (we typically use pdf-over-x.y.z)
    -> new snapshot version (new version with -SNAPSHOT suffix)
  -> it will then update version to release version, commit this & tag it with your release tag, update version to new snapshot version, and also commit this
  -> to build an installer, make sure you manually check out the release tag afterwards, before you proceed!

==== INSTALLER CREATION (you can do this at any time if you want to test a finalized bundle, you don't need to do it on a release tag, it's independent from the previous section)
DO THIS ON LINUX! there are some weird quirks on other platforms, cf. issue #62 (if you are on windows, setup WSL, it works perfectly fine there)
  -> ./publish.sh -Dks-pass={keystore password} -Dks-file={path to jks keystore file}
    -> if you only want specific binaries, use ./publish.sh --profiles {windows|linux|mac} {remaining cli arguments as above}
  -> this produces pdf-over-build/pdf-over_{windows|linux|mac}-{x86_64|aarch64}.{zip|jar|tar.gz} files
  -> each platform setup has to be post-processed specifically, see respective section(s) below

==== LINUX-SPECIFIC RELEASE PROCESS
  nothing to do here, just rename pdf-over_linux-x86_64.jar to PDF-Over-{version}.jar and call it a day

==== WINDOWS-SPECIFIC RELEASE PROCESS

step 1: unpack zip archive
  -> unpack to a temp directory, e.g. C:/temp/pdfover-packaging
  -> check that it runs
  -> try if pdf-over.exe works

step 2: create signed installer
  -> open pdf-over/installer/windows/PDF-Over.aip in Advanced Installer
  -> update Product Details
    -> Product version
    -> attempt to switch away from page
    -> when prompted, choose "Major Upgrade"
  -> make sure the project is still valid (these *should* be correct, but...)
    -> ensure that current files are included (Files and Folders)
      -> include exactly the contents of the release zip
    -> create shortcuts in "Start Menu -> Programs", "Desktop", and "Send To"
    -> ensure Digital Signature configuration is correct
      -> Use the A-SIT code signing cert from your cert store
      -> We currently use http://timestamp.digicert.com for timestamping
      -> SHA256 digest (or other appropriate choice)
    -> ensure that Launch Conditions excludes 32-bit systems
    -> ensure that the correct registry keys are setup (they already should be)
      -> HKEY_CLASSES_ROOT.pdf\OpenWithProgids, REG_SZ `PDF-Over.pdf` `(empty value)`
      -> HKEY_CLASSES_ROOT\Applications\PDF-Over.exe, REG_SZ `FriendlyAppName` `PDF-Over`
      -> HKEY_CLASSES_ROOT\Applications\PDF-Over.exe\DefaultIcon, REG_SZ `(empty key)` `"[reference to PDFOver.exe]",0`
      -> HKEY_CLASSES_ROOT\Applications\PDF-Over.exe\shell\open\command, REG_SZ `(empty key)` `"[reference to PDFOver.exe]" "%1"`
      -> HKEY_CLASSES_ROOT\Applications\PDF-Over.exe\SupportedTypes, REG_SZ `.pdf` `(empty value)`
      -> HKEY_CLASSES_ROOT\PDF-Over.pdf, REG_SZ `(empty key)` `PDF-Over`
      -> HKEY_CLASSES_ROOT\PDF-Over.pdf\DefaultIcon, REG_SZ `(empty key)` `"[reference to PDFOver.exe]",0`
      -> HKEY_CLASSES_ROOT\PDF-Over.pdf\shell\open\command `(empty key)` `"[reference to PDFOver.exe]" "%1"`
      -> Click "Build" in the toolbar, this produces pdf-over/installer/windows/PDF-Over-SetupFiles/PDF-Over.msi
      -> rename this to PDF-Over-{version}.msi; you are done

==== MAC-SPECIFIC RELEASE PROCESS
== (must be done on a Mac VM)

step 0: make new working directory
  mkdir /tmp/pdfover-packaging

step 1: unpack archive bundle
  unzip pdf-over_mac-x86_64.zip -d /tmp/pdfover-packaging
  -> unzip to /tmp/pdfover-packaging
  -> if you want to open this folder, use cmd+shift+g (the "go to" dialog)
  -> set necessary files to be executable
    -> cd /tmp/pdfover-packaging/
    -> chmod +x *.sh jre/bin/*
    -> check that it runs
      -> ./pdf-over_mac.sh
      -> (if you need to give permissions to java, navigate there in finder, then right click open to reveal the "OK" button)

step 2: use platypus to create a bundle app
  -> open platypus
  -> set Script Path "/tmp/pdfover-packaging/pdf-over_mac.sh"
  -> set App Name "PDF-Over"
  -> set Interface "None"
    -> for debugging, it can be useful to set this to "Text Window", and enable "Remain running after execution"
  -> set Icon
    -> Select .icns File
    -> /tmp/pdfover-packaging/icons/icon.icns
  -> set Identifier "at.a-sit.PDF-Over"
  -> set Author "A-SIT"
  -> set Version to current version
  -> check "Accepts dropped items", click Settings, check "Accept dropped files"
  -> add "jre", "lib" and "icons" (from /tmp/pdfover-packaging) to "Bundled Files"
  -> click "Create App", this creates a new .app
  -> save it in a new subfolder, as /tmp/pdfover-packaging/platypus/PDF-Over.app
  -> check that it runs
    -> (double-click the app in Finder)

step 3: sign the platypus bundle
  -> run ./signscript.sh platypus
  -> it needs a .app in a folder, that's why we made a subfolder :) no, don't ask me why, and feel free to invest time into fixing it
  -> this creates a signed app as /tmp/pdfover-packaging/pdf-over-{timestamp}/PDF-Over/PDF-Over.app

step 4: prepare disk image
  -> open disk utility
  -> click file (at the top of your screen!), new image, blank image
    -> name "PDF-Over", image format "Sparse Bundle Disk Package", size 250MB
    -> save as /tmp/pdfover-packaging/pdf-over.sparsebundle
  -> this has automatically mounted the bundle as a disk (in finder sidebar)
  -> drag the following into this bundle:
    -> the signed PDF-Over bundle from step 3
    -> a shortcut to the Applications folder
    -> background.png
  -> move background.png to an invisible folder
    -> Terminal: cd /Volumes/PDF-Over && mkdir .background && mv background.png .background
  -> switch to the "PDF-Over" volume in Finder
  -> switch Finder to icon view (if it isn't yet - it's the button to the right of the current folder name)
  -> in Finder, click "View" (at the top), "Show View Options", enable "Always open in icon view" and "Browse in icon view"
  -> set Background to "Picture", click to select, cmd+shift+g into /Volumes/PDF-Over/.background, and select background.png
  -> adjust icon size & grid spacing until it looks centered

step 5: bundle & notarize disk image
  -> unmount the "PDF-Over" volume if it is still mounted
  -> in Disk Utility, go to (at the top) Images, Convert
    -> select /tmp/pdfover-packaging/pdf-over.sparsebundle from step 4 as input
    -> set Image Format to "Compressed"
    -> save as /tmp/pdfover-packaging/PDF-Over-{version}.dmg
  -> mount it, verify that it looks good, then unmount it
  -> submit the bundle to app for notarization
    -> the first time, you will need to set your username & password
    -> run ./notarizeAppCommand.sh PDF-Over-{version}.dmg
    -> wait until notarization succeeds (usually <15min)
    -> you can check with ./notarizeCheckSingleCommand.sh {uuid}
  -> bundle the notarization ticket with the .dmg
    -> xcrun stapler staple PDF-Over-{version}.dmg
  -> you are done, this is the official mac release bundle!

==== PUBLISHING INSTALLERS TO https://technology.a-sit.at/en/pdf-over/
  -> log in to https://technology.a-sit.at/wp-admin/
    -> LDAP login without @, if it doesn't work, talk to admins
    -> click on "Downloads" in the sidebar
    -> if you can't see it, you are missing permissions, talk to admins (again)
    -> for each of the three PDF-Over downloads, click the title to edit, then...
      -> scroll down, click "add download"
      -> set correct version
      -> click upload file
      -> upload the respective installer file

==== PUBLISHING INSTALLERS TO JOINUP
  -> log into https://joinup.ec.europa.eu/
  -> go to the pdf-over project page
  -> "+", add release
    -> set Name to "PDF-Over"
    -> set Release number to {version}
    -> add Release notes (orient yourself on previous versions')
    -> leave rest unchanged
    -> save as draft
  -> use "+", add distribution to add setup files
  -> publish the release
    -> "..." in the far top right (inside the pdf-over banner)
    -> edit
    -> publish

==== NOTIFYING CLIENTS OF THE UPDATE
  -> sftp into https://updates.a-sit.at/
    -> talk to admins about login (asitupdates@websites.iaik.tugraz.at)
  -> update /html/pdf-over/Release.txt to the new version number
