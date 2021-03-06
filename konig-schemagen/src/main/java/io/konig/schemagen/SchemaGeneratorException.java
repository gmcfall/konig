package io.konig.schemagen;

public class SchemaGeneratorException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public SchemaGeneratorException(String message) {
		super(message);
	}
	
	public SchemaGeneratorException(Throwable cause) {
		super(cause);
	}
	
	public SchemaGeneratorException(String message, Throwable cause) {
		super(message, cause);
	}

}
