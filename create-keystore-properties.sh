#!/bin/bash
# Script para crear el archivo keystore.properties

echo "Configuración del Keystore para firmar el AAB"
echo "=============================================="
echo ""

read -sp "Ingresa la contraseña del keystore (storePassword): " STORE_PASSWORD
echo ""
read -p "Ingresa el alias de la clave (keyAlias) [default: key]: " KEY_ALIAS
KEY_ALIAS=${KEY_ALIAS:-key}
read -sp "Ingresa la contraseña de la clave (keyPassword): " KEY_PASSWORD
echo ""

cat > keystore.properties << EOF
storeFile=app/key.jks
storePassword=$STORE_PASSWORD
keyAlias=$KEY_ALIAS
keyPassword=$KEY_PASSWORD
EOF

echo ""
echo "✓ Archivo keystore.properties creado exitosamente"
echo ""

