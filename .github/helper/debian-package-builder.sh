#!/bin/bash
set -o errexit   # abort on nonzero exitstatus
set -o nounset   # abort on unbound variable
set -o pipefail  # don't hide errors within pipes


# config:
NAME="pdf-over-nightly"
FULLNAME="PDF-Over Nightly"
VERSION="`date +"%Y%m%d.%H%M%S"`"
ARCH="all"
JAR_TARGET="/usr/share/java/$NAME"


pwd
cd ./pdf-over-build
ls


jar_files=""
cd lib
for file in *.jar; do
    if [ -f "$file" ]; then
        jar_files+="lib/$file=$JAR_TARGET/$file "
    fi
done
cd ..


cat > $NAME.sh << EOF
#!/bin/sh 
# PDF-Over ($NAME $VERSION) launcher, generated on `date`

GDK_BACKEND=x11,wayland exec java -cp "$JAR_TARGET/*" at.asit.pdfover.gui.Main "\$@"
EOF
chmod +x $NAME.sh
 

cat > $NAME.desktop << EOF
[Desktop Entry]
Version=$VERSION
Type=Application
Name=$FULLNAME ($VERSION)
Comment=Create PAdES conforming PDF signatures
Exec=$NAME
Icon=$NAME
Terminal=false
StartupNotify=false
Categories=Office
EOF

fpm --version

fpm \
  -s dir -t deb \
  -p $NAME-$VERSION-$ARCH.deb \
  --name $NAME \
  --license EUPL-1.2 \
  --version $VERSION \
  --architecture $ARCH \
  --deb-upstream-changelog debian/changelog \
  --depends bash --depends "java-runtime (>= 21)" \
  --description "PDF-Over is your tool for frequent & efficient PDF signing." \
  --url "https://technology.a-sit.at/en/pdf-over/" \
  --vendor "A-SIT <software@egiz.gv.at>" \
  --maintainer "A-SIT <software@egiz.gv.at>" \
  $jar_files \
  $NAME.sh=/usr/bin/$NAME \
  $NAME.desktop=/usr/share/applications/$NAME.desktop \
  icons/icon144x144.png=/usr/share/pixmaps/$NAME.png

# next: upload/publish $NAME-$VERSION-$ARCH.deb

#mkdir experiments
#mv $NAME-$VERSION-$ARCH.deb experiments
#cd experiments
#ar x $NAME-$VERSION-$ARCH.deb
