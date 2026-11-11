#!/usr/bin/env bash
# Ganti placeholder "ifs00000" dengan username Anda. Pemakaian: ./rename.sh ifs18xyz
set -euo pipefail
NEW="${1:?Pemakaian: ./rename.sh <username>   contoh: ./rename.sh ifs18005}"
OLD="ifs00000"
[[ "$NEW" =~ ^[a-z][a-z0-9]*$ ]] || { echo "Username harus huruf kecil/angka, diawali huruf"; exit 1; }

# 1) isi file teks
grep -rIl "$OLD" . --exclude-dir=.git --exclude-dir=target --exclude=rename.sh \
  | xargs -r sed -i "s/$OLD/$NEW/g"

# 2) nama folder package (main + test)
for base in src/main/java/org/delcom src/test/java/org/delcom; do
  [ -d "$base/${OLD}springbootcashflow" ] && mv "$base/${OLD}springbootcashflow" "$base/${NEW}springbootcashflow"
done
echo "Selesai. Ubah juga nama folder proyek menjadi ${NEW}-p5-pbo2026-springboot-cashflow"
