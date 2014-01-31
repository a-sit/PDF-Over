Erzeugen der webstart-installer mit publish-script oder mit händischem
Aktivieren der jeweiligen Profile:
mvn install -P (linux/windows/mac)

Signieren mittels Token mit ... -P pkcs11-sign -Dpkcs11-pass=(...)

Windows-Installer:
	Advanced-Installer-Projektdateien enthalten:
	* PDF-Over.aip - Einfaches MSI, mit Free-Version erzeugbar
	* PDF-Over-signed.aip - Benötigt Pro-Version, signiert, Sprachen
	* User Interface -> Dialogs -> ShortcutsDlg
		* "Show Desktop Option" & "Checked by default" aktivieren

Mac-DMG-Paket:
	* Per webstart installieren, Paketinhalt entpacken
	* pdf-over_mac.sh script bearbeiten:
		* "$BASEDIR/../lib/*" in "$BASEDIR/lib/*" ändern
	* App erstellen mit Platypus: Screenshots platypus (drop settings).png
		* Beispiel-Profil inkludiert
	* DMG-Image erzeugen mit DmgPress: Screenshot DmgPress.png
		* Hintergrund für Folder: Background.png
		* Darstellung -> Als Liste; Darstellung -> Als Symbole
		* App + Link zu Applications-Folder hineinziehen (Screenshot)
