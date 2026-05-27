#!/bin/bash
ASTC_DIR="assets/minecraft/astc_cache"
PNG_DIR="src/main/resources/assets/minecraft/textures"

find "$ASTC_DIR" -type f -name "*.astc" | while read astc; do
  base=$(basename "$astc" .astc)
  png=$(find "$PNG_DIR" -type f -name "${base}.png" | head -1)
  if [ -n "$png" ]; then
    astc_out="${png%.png}.astc"
    mkdir -p "$(dirname "$astc_out")"
    cp "$astc" "$astc_out"
    echo "Movido: $base.astc -> ${astc_out#src/main/resources/assets/}"
  else
    echo "AVISO: Não achei PNG pra $base.astc"
  fi
done
