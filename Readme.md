# [PDF-Over](https://technology.a-sit.at/en/pdf-over/) 


[![pipeline status](https://gitlab.iaik.tugraz.at/egiz/pdf-over/badges/master/pipeline.svg)](https://gitlab.iaik.tugraz.at/egiz/pdf-over/-/commits/master)

## Release Notes 

### PDF-Over 4.3.0
- Support of the following signature profiles:
  + Standard Signature Block (the signature block as before)
  + Official Signature (this profile is used by public services for creating an official signature)
  + Logo Only (this profile can be used to sign with just only a logo as signature block)
  + Invisible (in this profile, the signature block is invisible)
- Support of new signature placeholder fields
  + This placeholder fields can be created by using e.g. Acrobat Reader or other software
  + The option to use can be set in the advanced configuration
- Support custom postfix
  + The postfix of the signed file to save can be set in the advanced configuration settings

Inquiries to: software@egiz.gv.at

Important: The Linux installer requires Java 8 or Java 11.

SHA-256 hash sum for version 4.3.1 Windows Installer:
97DE1C7618A504569195077C8E514780AD2F1C057AB18C935B80E3BBD784C5CB