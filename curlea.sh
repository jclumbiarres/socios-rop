curl -X POST http://localhost:8080/api/v1/socios/add \    
  -H "Content-Type: application/json" \
  -d '{
    "dni": "12345678A",
    "numero": 1,
    "nombre": "Juan Pérez",
    "fechaNacimiento": "1995-05-01",
  }'