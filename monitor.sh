#!/bin/bash

# Verificar que se proporcione el nombre del proceso
if [ $# -ne 1 ]; then
    echo "Uso: $0 <nombre_proceso> (ej: java, nginx, node, etc.)"
    exit 1
fi

PROCESO=$1
PID=$1

# Si no se encuentra el proceso
if [ -z "$PID" ]; then
    echo "Error: No se encontró el proceso '$PROCESO'"
    exit 1
fi

# Bucle de monitoreo en tiempo real (1 segundo por muestra)
while true; do
    # Obtener % de CPU (promedio desde inicio) y RAM en KB
    CPU=$(ps -p $PID -o %cpu= | awk '{print $1}')
    RSS=$(ps -p $PID -o rss= | awk '{print $1}')
    
    # Convertir RAM a MB (redondeado a 1 decimal)
    RAM_MB=$(echo "scale=1; $RSS / 1024" | bc)
    
    # Si el proceso terminó, salir
    if [ -z "$CPU" ] || [ -z "$RSS" ]; then
        echo "Error: El proceso $PID ya no existe"
        exit 1
    fi
    
    # Limpiar la pantalla para mostrar solo la última muestra
    clear
    
    # Mostrar el resultado
    echo "========================================"
    echo "  MONITOREO EN TIEMPO REAL - $PROCESO"
    echo "  ========================================"
    echo "  PID: $PID"
    echo "  %CPU: ${CPU}%"
    echo "  RAM: ${RAM_MB} MB"
    echo "  ========================================"
    echo "  (Presiona Ctrl+C para detener)"
    echo "========================================"
    
    # Esperar 1 segundo
    sleep 1
done