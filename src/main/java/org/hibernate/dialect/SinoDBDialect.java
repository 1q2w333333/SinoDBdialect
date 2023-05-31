package org.hibernate.dialect;

import java.sql.SQLException;

import org.hibernate.MappingException;
import org.hibernate.dialect.function.*;
import org.hibernate.exception.spi.TemplatedViolatedConstraintNameExtracter;
import org.hibernate.exception.spi.ViolatedConstraintNameExtracter;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.hibernate.internal.util.JdbcExceptionHelper;

public class SinoDBDialect extends Dialect {
	public SinoDBDialect() {
		registerColumnType(-5, "int8");
		registerColumnType(-2, "byte");
		registerColumnType(-7, "smallint");
		registerColumnType(1, "char($l)");
		registerColumnType(91, "date");
		registerColumnType(3, "decimal");
		registerColumnType(8, "float");
		registerColumnType(6, "smallfloat");
		registerColumnType(4, "integer");
		registerColumnType(-4, "blob");
		registerColumnType(-1, "clob");
		registerColumnType(2, "decimal");
		registerColumnType(7, "smallfloat");
		registerColumnType(5, "smallint");
		registerColumnType(93, "datetime year to fraction(5)");
		registerColumnType(92, "datetime hour to second");
		registerColumnType(-6, "smallint");
		registerColumnType(-3, "byte");
		registerColumnType(12, "varchar($l)");
		registerColumnType(12, 255, "varchar($l)");
		registerColumnType(12, 32739, "lvarchar($l)");
		registerFunction("concat", (SQLFunction) new VarArgsSQLFunction((Type) StandardBasicTypes.STRING, "(", "||", ")"));
	}


	@Override
	public String getAddColumnString() {
		return " add ";
	}

	public boolean supportsIdentityColumns() {
		return true;
	}

	public String getIdentitySelectString(String table, String column, int type) throws MappingException {
		return (type == -5) ? "select dbinfo('serial8') from systables where tabid=1" : "select dbinfo('sqlca.sqlerrd1') from systables where tabid=1";
	}

	public String getIdentityColumnString(int type) throws MappingException {
		return (type == -5) ? "serial8 not null" : "serial not null";
	}

	public boolean hasDataTypeInIdentityColumn() {
		return false;
	}

	@Override
	public String getAddPrimaryKeyConstraintString(String constraintName) {
		return " add constraint primary key constraint " + constraintName + " ";
	}

	@Override
	public String getCreateSequenceString(String sequenceName) {
		return "create sequence " + sequenceName;
	}

	@Override
	public String getDropSequenceString(String sequenceName) {
		return "drop sequence " + sequenceName + " restrict";
	}

	@Override
	public String getSequenceNextValString(String sequenceName) {
		return "select " + getSelectSequenceNextValString(sequenceName) + " from systables where tabid=1";
	}


	@Override
	public String getSelectSequenceNextValString(String sequenceName) {
		return sequenceName + ".nextval";
	}
	@Override
	public boolean supportsSequences() {
		return true;
	}
	@Override
	public boolean supportsLimit() {
		return true;
	}
	@Override
	public boolean useMaxForLimit() {
		return true;
	}
	@Override
	public boolean supportsLimitOffset() {
		return false;
	}
	@Override
	public String getLimitString(String querySelect, int offset, int limit) {
		if (offset > 0)
			throw new UnsupportedOperationException("query result offset is not supported");
		return (new StringBuffer(querySelect.length() + 8)).append(querySelect).insert(querySelect.toLowerCase().indexOf("select") + 6, " first " + limit).toString();
	}
	@Override
	public boolean supportsVariableLimit() {
		return false;
	}
	@Override
	public ViolatedConstraintNameExtracter getViolatedConstraintNameExtracter() {
		return EXTRACTER;
	}
	private static ViolatedConstraintNameExtracter EXTRACTER = (ViolatedConstraintNameExtracter) new TemplatedViolatedConstraintNameExtracter() {
		public String extractConstraintName(SQLException sqle) {
			String constraintName = null;
			int errorCode = JdbcExceptionHelper.extractErrorCode(sqle);
			if (errorCode == -268) {
				constraintName = extractUsingTemplate("Unique constraint (", ") violated.", sqle.getMessage());
			} else if (errorCode == -691) {
				constraintName = extractUsingTemplate("Missing key in referenced table for referential constraint (", ").", sqle.getMessage());
			} else if (errorCode == -692) {
				constraintName = extractUsingTemplate("Key value for constraint (", ") is still being referenced.", sqle.getMessage());
			}
			if (constraintName != null) {
				int i = constraintName.indexOf('.');
				if (i != -1)
					constraintName = constraintName.substring(i + 1);
			}
			return constraintName;
		}

		protected  String doExtractConstraintName(SQLException var1) throws NumberFormatException{
		  return "";
		}
	};
	@Override
	public boolean supportsCurrentTimestampSelection() {
		return true;
	}
	@Override
	public boolean isCurrentTimestampSelectStringCallable() {
		return false;
	}
	@Override
	public String getCurrentTimestampSelectString() {
		return "select distinct current timestamp from informix.systables";
	}
}