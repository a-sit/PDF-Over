Erzeugen der webstart-installer mit publish-script oder mit händischem
Aktivieren der jeweiligen Profile:
mvn install -P (linux/windows/mac)

Signieren mittels Token mit ... -P pkcs11-sign -Dpkcs11-pass=(...)

Windows-Installer:
	Advanced-Installer-Projektdateien enthalten:
	* PDF-Over.aip - Einfaches MSI, mit Free-Version erzeugbar
	* PDF-Over-signed.aip - Benötigt Pro-Version, signiert, Sprachen

Mac-DMG-Paket:
