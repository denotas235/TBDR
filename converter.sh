#!/bin/bash
# EngenhariaSonora: Conversor ASTC Global
# Uso: ./converter.sh

SRC_DIR="assets/minecraft/textures"
CACHE_DIR="assets/minecraft/astc_cache"
META_FILE="$CACHE_DIR/metadata.txt"

mkdir -p "$CACHE_DIR"
echo "Iniciando compressão ASTC global..." >&2
echo "" > "$META_FILE"

# Procura todos os .png recursivamente dentro da pasta de texturas
find "$SRC_DIR" -name "*.png" | while read -r img; do
    nome=$(basename "$img" .png)
    dir_path=$(dirname "$img")
    
    # Lógica de seleção de bloco baseada no diretório e nome
    if [[ "$dir_path" == *"entity"* ]]; then
        bloco="4x4" # Personagens e Mobs
    elif [[ "$dir_path" == *"block"* ]]; then
        bloco="4x4" # Blocos
    elif [[ "$dir_path" == *"gui"* || "$dir_path" == *"item"* ]]; then
        bloco="5x5" # UI e Itens
    elif [[ "$dir_path" == *"particle"* || "$dir_path" == *"environment"* ]]; then
        bloco="6x6" # Efeitos
    elif [[ "$dir_path" == *"sky"* ]]; then
        bloco="8x8" # Skybox
    else
        bloco="6x6" # Fallback
    fi
    
    # Conversão silenciosa
     -c "$img" "$CACHE_DIR/$nome.astc" $bloco -esw > /dev/null
    
    # Regista o nome da textura e o bloco usado
    echo "$nome:$bloco" >> "$META_FILE"
    echo "Processado: $nome ($bloco)"
done

echo "---"
echo "Conversão concluída. Cache gerado em: $CACHE_DIR"
