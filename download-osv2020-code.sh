#!/bin/bash
set -euxo pipefail

wget https://www.kiesraad.nl/binaries/kiesraad/documenten/publicaties/2026/02/02/broncode-osv2020-u---gr26/broncode+osv2020-u-versie-1.12.7.2.zip
unzip broncode+osv2020-u-versie-1.12.7.2.zip "osv2020-u-versie-1.12.7.2/*"
rm broncode+osv2020-u-versie-1.12.7.2.zip
