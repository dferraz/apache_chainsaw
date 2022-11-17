/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.log4j.db.dialect;

import org.apache.log4j.db.ConnectionSource;
import org.apache.log4j.spi.ComponentBase;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;


/**
 * @author Ceki Gulcu
 */
public class Util extends ComponentBase {
    private static final String POSTGRES_PART = "postgresql";
    private static final String MYSQL_PART = "mysql";
    private static final String ORACLE_PART = "oracle";
    //private static final String MSSQL_PART = "mssqlserver4";
    private static final String MSSQL_PART = "microsoft";
    private static final String HSQL_PART = "hsql";

    public static int discoverSQLDialect(DatabaseMetaData meta) {
        int dialectCode = 0;

        try {

            String dbName = meta.getDatabaseProductName().toLowerCase();

            if (dbName.contains(POSTGRES_PART)) {
                return ConnectionSource.POSTGRES_DIALECT;
            } else if (dbName.contains(MYSQL_PART)) {
                return ConnectionSource.MYSQL_DIALECT;
            } else if (dbName.contains(ORACLE_PART)) {
                return ConnectionSource.ORACLE_DIALECT;
            } else if (dbName.contains(MSSQL_PART)) {
                return ConnectionSource.MSSQL_DIALECT;
            } else if (dbName.contains(HSQL_PART)) {
                return ConnectionSource.HSQL_DIALECT;
            } else {
                return ConnectionSource.UNKNOWN_DIALECT;
            }
        } catch (SQLException sqle) {
            // we can't do much here
        }

        return dialectCode;
    }

    public static SQLDialect getDialectFromCode(int dialectCode) {
        SQLDialect sqlDialect = null;

        switch (dialectCode) {
            case ConnectionSource.POSTGRES_DIALECT:
                sqlDialect = new PostgreSQLDialect();

                break;
            case ConnectionSource.MYSQL_DIALECT:
                sqlDialect = new MySQLDialect();

                break;
            case ConnectionSource.ORACLE_DIALECT:
                sqlDialect = new OracleDialect();

                break;
            case ConnectionSource.MSSQL_DIALECT:
                sqlDialect = new MsSQLDialect();

                break;
            case ConnectionSource.HSQL_DIALECT:
                sqlDialect = new HSQLDBDialect();

                break;
        }
        return sqlDialect;
    }

    /**
     * This method handles cases where the
     * {@link DatabaseMetaData#supportsGetGeneratedKeys} method is missing in the
     * JDBC driver implementation.
     */
    public boolean supportsGetGeneratedKeys(DatabaseMetaData meta) {
        try {
            return meta.supportsGetGeneratedKeys();
        } catch (Throwable e) {
            getLogger().info("Could not call supportsGetGeneratedKeys method. This may be recoverable");
            return false;
        }
    }

    /**
     * This method handles cases where the
     * {@link DatabaseMetaData#supportsBatchUpdates} method is missing in the
     * JDBC driver implementation.
     */
    public boolean supportsBatchUpdates(DatabaseMetaData meta) {
        try {
            return meta.supportsBatchUpdates();
        } catch (Throwable e) {
            getLogger().info("Missing DatabaseMetaData.supportsBatchUpdates method.");
            return false;
        }
    }
}
