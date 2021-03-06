package io.konig.sql;

import org.openrdf.model.URI;

import io.konig.core.Path;

public class SQLColumnSchema {
	
	private SQLTableSchema columnTable;
	private String columnName;
	private SQLColumnType columnType;
	private SQLConstraint notNull;
	private SQLConstraint primaryKey;
	private UniqueConstraint unique;
	private URI columnPredicate;
	private Path equivalentPath;
	private ForeignKeyConstraint foreignKey;
	private NullConstraint nullConstraint;

	public SQLColumnSchema() {
		
	}

	public SQLColumnSchema(SQLTableSchema columnTable, String columnName, SQLColumnType columnType) {
		this.columnTable = columnTable;
		this.columnName = columnName;
		this.columnType = columnType;
		columnTable.addColumn(this);
	}
	


	public SQLColumnSchema(String columnName) {
		this.columnName = columnName;
	}

	public SQLColumnSchema(String columnName, SQLColumnType columnType) {
		this.columnName = columnName;
		this.columnType = columnType;
	}

	public SQLTableSchema getColumnTable() {
		return columnTable;
	}

	public void setColumnTable(SQLTableSchema columnTable) {
		this.columnTable = columnTable;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public SQLColumnType getColumnType() {
		return columnType;
	}

	public void setColumnType(SQLColumnType columnType) {
		this.columnType = columnType;
	}

	public boolean isNotNull() {
		return notNull != null;
	}

	public SQLConstraint getNotNull() {
		return notNull;
	}

	public void setNotNull(SQLConstraint notNull) {
		this.notNull = notNull;
	}
	
	public boolean isPrimaryKey() {
		return primaryKey != null;
	}

	public SQLConstraint getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(SQLConstraint primaryKey) {
		this.primaryKey = primaryKey;
	}

	public String getFullName() {
		StringBuilder builder = new StringBuilder();
		SQLSchema schema = columnTable.getSchema();
		String schemaName = schema==null ? "global" : schema.getSchemaName().toLowerCase();
		String tableName = columnTable.getTableName();
		builder.append(schemaName);
		builder.append('.');
		builder.append(tableName);
		builder.append('.');
		builder.append(columnName);
		
		return builder.toString();
	}

	public URI getColumnPredicate() {
		return columnPredicate;
	}

	public void setColumnPredicate(URI columnPredicate) {
		this.columnPredicate = columnPredicate;
	}

	public Path getEquivalentPath() {
		return equivalentPath;
	}

	public void setEquivalentPath(Path equivalentPath) {
		this.equivalentPath = equivalentPath;
	}

	public ForeignKeyConstraint getForeignKey() {
		return foreignKey;
	}

	public void setForeignKey(ForeignKeyConstraint foreignKey) {
		this.foreignKey = foreignKey;
	}
	
	public String toString() {
		return getFullName();
	}

	public UniqueConstraint getUnique() {
		return unique;
	}

	public void setUnique(UniqueConstraint unique) {
		this.unique = unique;
	}

	public NullConstraint getNullConstraint() {
		return nullConstraint;
	}

	public void setNullConstraint(NullConstraint nullConstraint) {
		this.nullConstraint = nullConstraint;
	}

}
