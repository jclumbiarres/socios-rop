# Socios

Pequeño gestor de socios para organizaciones como prueba de concepto en Java 25 con spring-boot 4.0.1

## Requisitos
- Java 25 GraalVM-CE
- Gradle 9.6.1
- Docker

## Diseño
El proyecto es un intento de ROP (Railway Oriented Programming) en Java, se ha creado un tipo ADT monádico `Result<T,E>` para manejar errores de forma funcional y evitar excepciones en tiempo de ejecución.

## Extras
- Virtual Threads (Loom)
- Spring Boot 4.0.1
- GraalVM Native Image
- sealed interfaces
- records
- pattern matching
