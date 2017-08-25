/**
 * Copyright 2017 Syncleus, Inc.
 * with portions copyright 2004-2017 Bo Zimmerman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.syncleus.aethermud.game.core.database;

import com.syncleus.aethermud.game.core.CMSecurity;
import com.syncleus.aethermud.game.core.Log;

import java.sql.*;


public class DBConnection {
    /** Whether this dbconnection is being used */
    protected boolean inUse;
    /** if any SQL errors occur, they are here. **/
    protected String lastError = null;
    /** number of failures in a row */
    protected int failuresInARow = 0;
    protected boolean sqlserver = false;
    protected boolean isReusable = false;
    /** for tracking the last sql statement made */
    protected String lastSQL = "";
    /** Connection object being used */
    private Connection myConnection = null;
    /** (new) resultset being used currently */
    private ResultSet myResultSet = null;
    /** (new) statement object being used currently */
    private Statement myStatement = null;
    /** (new) statement object being used currently */
    private PreparedStatement myPreparedStatement = null;
    /** last time the connection was queried/executed. **/
    private long lastQueryTime = System.currentTimeMillis();
    /** when this connection was put into use **/
    private long lastPutInUseTime = System.currentTimeMillis();
    /** parent container of this connection **/
    private DBConnections myParent = null;
    /** for remembering whether this is a fakeDB connection */
    private Boolean isFakeDB = null;

    /**
     * construction
     *
     * Usage: DBConnection("","","");
     * @param parent    the parent connections object
     * @param DBClass    JDBC Class
     * @param DBService    ODBC SERVICE
     * @param DBUser    ODBC LOGIN USERNAME
     * @param DBPass    ODBC LOGIN PASSWORD
     * @param DBReuse   Whether the connection can be reused.
     * @throws SQLException a sql error
     */
    public DBConnection(DBConnections parent,
                        String DBClass,
                        String DBService,
                        String DBUser,
                        String DBPass,
                        boolean DBReuse)
        throws SQLException {
        myParent = parent;
        if ((DBClass == null) || (DBClass.length() == 0))
            DBClass = "sun.jdbc.odbc.JdbcOdbcDriver";
        try {
            Class.forName(DBClass);
        } catch (final ClassNotFoundException ce) {
            ce.printStackTrace();
        }
        sqlserver = true;
        isReusable = DBReuse;
        final java.util.Properties p = new java.util.Properties();
        if ((DBUser != null) && (DBUser.length() > 0)) {
            p.put("user", DBUser);
            p.put("password", DBPass);
        }
        p.put("SetBigStringTryClob", "true");
        myConnection = DriverManager.getConnection(DBService, p);
        if (CMSecurity.isDebugging(CMSecurity.DbgFlag.SQLERRORS))
            Log.debugOut("New connection made to :" + DBService + " using " + DBClass);
        sqlserver = false;
        inUse = false;
    }

    public String catalog() {
        try {
            return myConnection.getCatalog();
        } catch (final Exception e) {
            if (CMSecurity.isDebugging(CMSecurity.DbgFlag.SQLERRORS)) {
                Log.errOut("DBConnection", e);
            }
        }
        return "";
    }

    public boolean isFakeDB() {
        if (isFakeDB == null) {
            final String catalog = catalog();
            if (catalog == null)
                isFakeDB = Boolean.FALSE;
            else
                isFakeDB = Boolean.valueOf(catalog.equalsIgnoreCase("FAKEDB"));
        }
        return isFakeDB.booleanValue();
    }

    /**
     * shut down this connection totally
     *
     * Usage: close()
     */
    public void close() {
        try {
            if (myStatement != null)
                myStatement.close();
        } catch (final SQLException e) {
            if (CMSecurity.isDebugging(CMSecurity.DbgFlag.SQLERRORS))
                Log.errOut("DBConnection", e);
        }
        try {
            if (myPreparedStatement != null)
                myPreparedStatement.close();
        } catch (final SQLException e) {
            if (CMSecurity.isDebugging(CMSecurity.DbgFlag.SQLERRORS))
                Log.errOut("DBConnection", e);
        }
        try {
            if (myConnection != null)
                myConnection.close();
        } catch (final SQLException e) {
            if (CMSecurity.isDebugging(CMSecurity.DbgFlag.SQLERRORS))
                Log.errOut("DBConnection", e);
        }
        myConnection = null;
        myStatement = null;
        myPreparedStatement = null;
        myParent = null;
    }

    /**
     * set up this connection for use
     *
     * Usage: use("begin transaction")
     * @param openerSQL    Any SQL string you'd like to send
     * @return boolean    The connection being used
     */
    public synchronized boolean use(String openerSQL) {
        if ((!inUse) && (ready()) && (!isProbablyDead())) {
            lastError = null;
            try {
                myPreparedStatement = null;
                sqlserver = true;
                myStatement = myConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            } catch (final SQLException e) {
                if (CMSecurity.isDebugging(CMSecurity.DbgFlag.SQLERRORS))
                    Log.errOut("DBConnection", e);
                myConnection = null;
                failuresInARow++;
                sqlserver = false;
                return false;
            }

            sqlserver = false;
            try {
                if (!openerSQL.equals("")) {
                    lastSQL = openerSQL;
                    lastQueryTime = System.currentTimeMillis();
                    myStatement.executeUpdate(openerSQL);
                }
            } catch (final SQLException e) {
                if (CMSecurity.isDebugging(CMSecurity.DbgFlag.SQLERRORS)) {
                    Log.errOut("DBConnection", "Error use: " + openerSQL);
                    Log.errOut("DBConnection", e);
                }
                return false;
                // not a real error?!
            }

            lastPutInUseTime = System.currentTimeMillis();
            inUse = true;
            return true;
        }
        return false;
    }

    /**
     * set up this connection for use
     *
     * Usage: useEmpty()
     * @return boolean    The connection being used
     */
    public synchronized boolean useEmpty() {
        if ((!inUse) && (ready()) && (!isProbablyDead())) {
            lastError = null;
            myPreparedStatement = null;
            sqlserver = true;
            myStatement = null;
            sqlserver = false;
            lastPutInUseTime = System.currentTimeMillis();
            inUse = true;
            return true;
        }
        return false;
    }

    /**
     * set up this connection for use as a prepared statement
     *
     * Usage: usePrepared("SQL String")
     * @param SQL    Any SQL string you'd like to use
     * @return boolean    The connection being used
     */
    public synchronized boolean usePrepared(String SQL) {
        if ((!inUse) && (ready())) {

            lastError = null;

            try {
                myStatement = null;
                sqlserver = true;
                lastSQL = SQL;
                myPreparedStatement = myConnection.prepareStatement(SQL);
                sqlserver = false;
            } catch (final SQLException e) {
                if (CMSecurity.isDebugging(CMSecurity.DbgFlag.SQLERRORS)) {
                    Log.errOut("DBConnection", "Error prepare: " + SQL);
                    Log.errOut("DBConnection", e);
                }
                sqlserver = false;
                myConnection = null;
                failuresInARow++;
                return false;
            }

            sqlserver = false;
            lastPutInUseTime = System.currentTimeMillis();
            failuresInARow = 0;
            inUse = true;
            return true;
        }
        return false;
    }

    /**
     * set up this connection for use as a prepared statement
     * Requires an already in use connection.
     *
     * Usage: rePrepare("SQL String")
     * @param SQL    Any SQL string you'd like to use
     * @return boolean    The connection being used
     */
    public synchronized boolean rePrepare(String SQL) {
        if (inUse) {
            closeStatements("");
            lastError = null;
            try {
                myStatement = null;
                sqlserver = true;
                lastSQL = SQL;
                myPreparedStatement = myConnection.prepareStatement(SQL);
                sqlserver = false;
            } catch (final SQLException e) {
                if (CMSecurity.isDebugging(CMSecurity.DbgFlag.SQLERRORS)) {
                    Log.errOut("DBConnection", "Error reprepare: " + SQL);
                    Log.errOut("DBConnection", e);
                } else if (("" + e).equals("null"))
                    Log.errOut("DBConnection", "Re-prepare error: null");
                else
                    Log.errOut("DBConnection", "Re-prepare error: " + e.getMessage());
                sqlserver = false;
                myConnection = null;
                failuresInARow++;
                return false;
            }

            sqlserver = false;
            lastPutInUseTime = System.currentTimeMillis();
            failuresInARow = 0;
            inUse = true;
            return true;
        }
        return false;
    }

    protected void closeStatements(String Closer) {
        try {
            if (!Closer.equals("")) {
                if (myStatement != null) {
                    lastSQL = Closer;
                    lastQueryTime = System.currentTimeMillis();
                    myStatement.executeUpdate(Closer);
                }
            }
            if (myResultSet != null) {
                myResultSet.close();
                myResultSet = null;
            }
            if (myPreparedStatement != null) {
                myPreparedStatement.close();
                myPreparedStatement = null;
            }
            if (myStatement != null) {
                myStatement.close();
                myStatement = null;
            }
            if (myConnection != null) {
                if (!myConnection.getAutoCommit())
                    myConnection.commit();
            }
        } catch (final SQLException e) {
            if (CMSecurity.isDebugging(CMSecurity.DbgFlag.SQLERRORS)) {
                Log.errOut("DBConnection", e);
            }
            // not a real error?
        }
    }

    /**
     * report this connection as being free
     *
     * Usage: doneUsing("roll back");
     * @param Closer    Any SQL string you'd like to send
     */
    protected void doneUsing(String Closer) {
        closeStatements(Closer);
        if (!isReusable)
            close();
        inUse = false;
    }

    /**
     * Return the time, in millis, when this connection
     * was last returned.
     * @return the last time a query was made on this conn
     */
    public long getLastQueryTime() {
        return lastQueryTime;
    }

    /**
     * execute a query, returning the resultset
     *
     * Usage: R=query("SELECT STATEMENT");
     * @param queryString    SQL query-style string
     * @return ResultSet    The results of the query
     * @throws SQLException a sql error
     */
    public ResultSet query(String queryString)
        throws SQLException {
        lastSQL = queryString;
        ResultSet R = null;
        if ((inUse) && (ready())) {
            try {
                sqlserver = true;
                lastQueryTime = System.currentTimeMillis();
                if (myStatement != null)
                    R = myStatement.executeQuery(queryString);
                else
                    lastError = "DBConnection Statement not open.";
                sqlserver = false;
            } catch (final SQLException sqle) {
                if (CMSecurity.isDebugging(CMSecurity.DbgFlag.SQLERRORS)) {
                    Log.errOut("DBConnection", "Error query: " + queryString);
                    Log.errOut("DBConnection", "" + sqle);
                } else if (("" + sqle).equals("null"))
                    Log.errOut("DBConnection", "Query error: null");
                else
                    Log.errOut("DBConnection", "Query error: " + sqle.getMessage());
                sqlserver = false;
                failuresInARow++;
                lastError = "" + sqle;
                if (isProbablyDead()) {
                    if (myParent != null)
                        myParent.resetConnections();
                }
                throw sqle;
            }
            sqlserver = false;
        } else {
            lastError = "DBConnection not ready.";
        }
        sqlserver = false;
        failuresInARow = 0;
        lastPutInUseTime = System.currentTimeMillis();
        if (myParent != null)
            myParent.clearErrors();
        myResultSet = R;
        return R;
    }

    /**
     * Sets all the clobs in the prepared statement to the given strings
     * @param vals the strings, in order
     * @throws SQLException a sql error
     */
    public void setPreparedClobs(String[] vals) throws SQLException {
        if (getPreparedStatement() == null) {
            return;
        }
        for (int t = 0; t < vals.length; t++) {
            if (vals[t] == null)
                getPreparedStatement().setNull(t + 1, java.sql.Types.CLOB);
            else
                getPreparedStatement().setString(t + 1, vals[t]);
        }
    }

    /**
     * execute an sql update, returning the status
     *
     * Usage: update("UPDATE STATEMENT");
     * @param updateString    SQL update-style string
     * @param retryNum    a retry number
     * @return int    The status of the update
     * @throws SQLException a sql error
     */
    public int update(String updateString, int retryNum)
        throws SQLException {
        lastSQL = updateString;
        int responseCode = -1;
        if ((inUse) && (ready())) {
            try {
                sqlserver = true;
                lastQueryTime = System.currentTimeMillis();
                if (myStatement != null)
                    responseCode = myStatement.executeUpdate(updateString);
                else if (myPreparedStatement != null)
                    responseCode = myPreparedStatement.executeUpdate();
                else {
                    myStatement = myConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    responseCode = myStatement.executeUpdate(updateString);
                }
                sqlserver = false;
            } catch (final SQLException sqle) {
                sqlserver = false;
                if ((sqle.getMessage() == null)
                    || (sqle.getMessage().toUpperCase().indexOf("PRIMARY KEY") < 0))
                    failuresInARow++;
                lastError = "" + sqle;
                if (CMSecurity.isDebugging(CMSecurity.DbgFlag.SQLERRORS)) {
                    Log.errOut("DBConnection", "Error update: " + updateString);
                    Log.errOut("DBConnection", sqle);
                } else
                    Log.errOut("DBConnection", updateString + ": " + sqle);
                if ((myParent != null) && (myStatement != null))
                    myParent.enQueueError(updateString, "" + sqle, "" + (retryNum + 1));
                if (isProbablyDead()) {
                    if (myParent != null)
                        myParent.resetConnections();
                }
                throw sqle;
            }
        }

        sqlserver = false;
        lastPutInUseTime = System.currentTimeMillis();
        failuresInARow = 0;
        if (myParent != null)
            myParent.clearErrors();
        return responseCode;
    }

    /**
     * returns whether this connection is ready for use
     *
     * Usage: ready();
     * @return boolean    Whether this connection is ready
     */
    public boolean ready() {
        return (myConnection != null);
    }

    /**
     * returns whether this connection is in use
     *
     * Usage: inUse();
     * @return boolean    Whether this connection is in use
     */
    public boolean inUse() {
        return inUse;
    }

    /**
     * Returns the number of records in the given result set.
     * @param R the result set
     * @return the number of records
     */
    public int getRecordCount(ResultSet R) {
        int recordCount = 0;
        try {
            R.last();
            recordCount = R.getRow();
            R.beforeFirst();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return recordCount;
    }

    /**
     * known errors should not be a reason to report a dead state
     *
     * Usage: clearFailures();
     */
    public void clearFailures() {
        failuresInARow = 0;
    }

    public boolean inSQLServerCommunication() {
        return sqlserver;
    }

    /**
     * returns whether this connection is *probably* dead
     *
     * Usage: isProbablyDead();
     * @return boolean    Whether this connection is probably dead
     */
    public boolean isProbablyDead() {
        try {
            if ((myConnection == null) || (myConnection.isClosed()) || (failuresInARow > 2))
                return true;
            return false;
        } catch (final SQLException e) {
            return true;
        }
    }

    /**
     * returns whether this connection is *probably* locked up
     *
     * Usage: isProbablyLockedUp();
     * @return boolean    Whether this connection is locked up
     */
    public boolean isProbablyLockedUp() {
        final long twominsAgo = System.currentTimeMillis() - (2 * 60 * 1000);
        if ((lastPutInUseTime < twominsAgo) && inUse)
            return true;
        return false;
    }

    /**
     * returns an error if there was one
     *
     * Usage: getLastError();
     * @return String    The last error SQL string, if any
     */
    public String getLastError() {
        if (lastError == null)
            return "";
        return lastError;
    }

    /**
     * returns the prepared statement, if creates
     *
     * Usage: getPreparedStatement();
     * @return PreparedStatement    the prepared statement
     */
    public PreparedStatement getPreparedStatement() {
        return myPreparedStatement;
    }

    public static enum FetchType {
        EMPTY,
        STATEMENT,
        PREPAREDSTATEMENT,
        TESTSTATEMENT
    }
}
