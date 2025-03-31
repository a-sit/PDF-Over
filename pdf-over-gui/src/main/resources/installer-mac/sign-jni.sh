cd lib
jar xf jna-5.12.1.jar
codesign -s "Developer ID Application: SIT Zentrum fuer sichere Informationstechnologie-Austria (9CYHJNG644)" -f -vvvv  --timestamp -o runtime "com/sun/jna/darwin-x86-64/libjnidispatch.jnilib"
codesign -s "Developer ID Application: SIT Zentrum fuer sichere Informationstechnologie-Austria (9CYHJNG644)" -f -vvvv  --timestamp -o runtime "com/sun/jna/aarch64/libjnidispatch.jnilib"
jar -uf jna-5.12.1.jar com/sun/jna/darwin-x86-64/libjnidispatch.jnilib
jar -uf jna-5.12.1.jar com/sun/jna/darwin-aarch64/libjnidispatch.jnilib
rm -rf com META-INF
cd ..
