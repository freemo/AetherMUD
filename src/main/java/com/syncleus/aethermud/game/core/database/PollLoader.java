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
package com.planet_ink.game.core.database;

import com.planet_ink.game.Libraries.interfaces.DatabaseEngine;
import com.planet_ink.game.core.CMLib;
import com.planet_ink.game.core.Log;

import java.sql.ResultSet;
import java.util.List;
import java.util.Vector;


public class PollLoader {
    protected DBConnector DB = null;

    public PollLoader(DBConnector newDB) {
        DB = newDB;
    }

    public DatabaseEngine.PollData DBRead(String name) {
        DBConnection D = null;
        try {
            D = DB.DBFetch();
            final ResultSet R = D.query("SELECT * FROM CMPOLL WHERE CMNAME='" + name + "'");
            while (R.next()) {
                final String cname = DBConnections.getRes(R, "CMNAME");
                final String byName = DBConnections.getRes(R, "CMBYNM");
                final String subject = DBConnections.getRes(R, "CMSUBJ");
                final String description = DBConnections.getRes(R, "CMDESC");
                final String options = DBConnections.getRes(R, "CMOPTN");
                final long flag = DBConnections.getLongRes(R, "CMFLAG");
                final String qual = DBConnections.getRes(R, "CMQUAL");
                final String results = DBConnections.getRes(R, "CMRESL");
                final long expiration = DBConnections.getLongRes(R, "CMEXPI");

                final DatabaseEngine.PollData data = new DBInterface.PollData() {

                    @Override
                    public String name() {
                        return cname;
                    }

                    @Override
                    public long flag() {
                        return flag;
                    }

                    @Override
                    public String byName() {
                        return byName;
                    }

                    @Override
                    public String subject() {
                        return subject;
                    }

                    @Override
                    public String description() {
                        return description;
                    }

                    @Override
                    public String options() {
                        return options;
                    }

                    @Override
                    public String qual() {
                        return qual;
                    }

                    @Override
                    public String results() {
                        return results;
                    }

                    @Override
                    public long expiration() {
                        return expiration;
                    }

                };
                return data;
            }
        } catch (final Exception sqle) {
            Log.errOut("PollLoader", sqle);
        } finally {
            DB.DBDone(D);
        }
        // log comment
        return null;
    }

    public List<DatabaseEngine.PollData> DBReadList() {
        DBConnection D = null;
        final Vector<DatabaseEngine.PollData> rows = new Vector<DatabaseEngine.PollData>();
        try {
            D = DB.DBFetch();
            final ResultSet R = D.query("SELECT * FROM CMPOLL");
            while (R.next()) {
                final String cname = DBConnections.getRes(R, "CMNAME");
                final long flag = DBConnections.getLongRes(R, "CMFLAG");
                final String qual = DBConnections.getRes(R, "CMQUAL");
                final long expiration = DBConnections.getLongRes(R, "CMEXPI");

                final DatabaseEngine.PollData data = new DBInterface.PollData() {

                    @Override
                    public String name() {
                        return cname;
                    }

                    @Override
                    public long flag() {
                        return flag;
                    }

                    @Override
                    public String byName() {
                        return "";
                    }

                    @Override
                    public String subject() {
                        return "";
                    }

                    @Override
                    public String description() {
                        return "";
                    }

                    @Override
                    public String options() {
                        return "";
                    }

                    @Override
                    public String qual() {
                        return qual;
                    }

                    @Override
                    public String results() {
                        return "";
                    }

                    @Override
                    public long expiration() {
                        return expiration;
                    }

                };
                rows.addElement(data);
            }
        } catch (final Exception sqle) {
            Log.errOut("PollLoader", sqle);
        } finally {
            DB.DBDone(D);
        }
        // log comment
        return rows;
    }

    public void DBUpdate(String OldName,
                         String name,
                         String player,
                         String subject,
                         String description,
                         String optionXML,
                         int flag,
                         String qualZapper,
                         String results,
                         long expiration) {
        DB.updateWithClobs(
            "UPDATE CMPOLL SET"
                + " CMRESL=?"
                + " WHERE CMNAME='" + OldName + "'", results + " ");

        DB.updateWithClobs(
            "UPDATE CMPOLL SET"
                + "  CMNAME='" + name + "'"
                + ", CMBYNM='" + player + "'"
                + ", CMSUBJ='" + subject + "'"
                + ", CMDESC=?"
                + ", CMOPTN=?"
                + ", CMFLAG=" + flag
                + ", CMQUAL='" + qualZapper + "'"
                + ", CMEXPI=" + expiration
                + "  WHERE CMNAME='" + OldName + "'", new String[][]{{description + " ", optionXML + " "}});

    }

    public void DBUpdate(String name, String results) {
        DB.updateWithClobs(
            "UPDATE CMPOLL SET"
                + " CMRESL=?"
                + " WHERE CMNAME='" + name + "'", results + " ");
    }

    public void DBDelete(String name) {
        DB.update("DELETE FROM CMPOLL WHERE CMNAME='" + name + "'");
        CMLib.s_sleep(500);
        if (DB.queryRows("SELECT * FROM CMPOLL WHERE CMNAME='" + name + "'") > 0)
            Log.errOut("Failed to delete data from poll " + name + ".");
    }

    public void DBCreate(String name,
                         String player,
                         String subject,
                         String description,
                         String optionXML,
                         int flag,
                         String qualZapper,
                         String results,
                         long expiration) {
        DB.updateWithClobs(
            "INSERT INTO CMPOLL ("
                + "CMNAME, "
                + "CMBYNM, "
                + "CMSUBJ, "
                + "CMDESC, "
                + "CMOPTN, "
                + "CMFLAG, "
                + "CMQUAL, "
                + "CMRESL, "
                + "CMEXPI "
                + ") values ("
                + "'" + name + "',"
                + "'" + player + "',"
                + "'" + subject + "',"
                + "?, "
                + "?,"
                + "" + flag + ","
                + "'" + qualZapper + "',"
                + "?,"
                + "" + expiration + ""
                + ")", new String[][]{{description, optionXML, results + " "}});
    }
}