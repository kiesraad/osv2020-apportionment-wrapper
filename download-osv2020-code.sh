#!/bin/bash

wget https://www.kiesraad.nl/binaries/kiesraad/documenten/formulieren/2024/4/23/broncode-osv2020-u-europees-parlementsverkiezing-2024/osv2020-u-versie-1.10.5.1.zip
unzip osv2020-u-versie-1.10.5.1.zip osv2020-u-versie-1.10.5.1.zip -d osv2020-u-versie-1.10.5.1
pushd osv2020-u-versie-1.10.5.1
unzip osv2020-u-versie-1.10.5.1.zip
rm osv2020-u-versie-1.10.5.1.zip
popd
rm osv2020-u-versie-1.10.5.1.zip
