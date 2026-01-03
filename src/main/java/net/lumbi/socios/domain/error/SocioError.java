package net.lumbi.socios.domain.error;

public sealed interface SocioError {
    record DNIAlreadyExists(String dni) implements SocioError {
    }

    record NumeroAlreadyExists(Integer numero) implements SocioError {
    }

    record NombreAlreadyExists(String nombre) implements SocioError {
    }

    record EmptyField(String field) implements SocioError {
    }

    record DatabaseBoom(String details) implements SocioError {
    } // Para fallos técnicos encapsulados
}