package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.CMFile.CMVFSFile;
import com.planet_ink.coffee_mud.core.database.ClanLoader;
import com.planet_ink.coffee_mud.core.database.DBConnections;
import com.planet_ink.coffee_mud.core.database.DBConnector;
import com.planet_ink.coffee_mud.core.database.DataLoader;
import com.planet_ink.coffee_mud.core.database.GAbilityLoader;
import com.planet_ink.coffee_mud.core.database.GCClassLoader;
import com.planet_ink.coffee_mud.core.database.GRaceLoader;
import com.planet_ink.coffee_mud.core.database.JournalLoader;
import com.planet_ink.coffee_mud.core.database.MOBloader;
import com.planet_ink.coffee_mud.core.database.PollLoader;
import com.planet_ink.coffee_mud.core.database.QuestLoader;
import com.planet_ink.coffee_mud.core.database.RoomLoader;
import com.planet_ink.coffee_mud.core.database.StatLoader;
import com.planet_ink.coffee_mud.core.database.VFSLoader;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.MemberRecord;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2016 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

/**
 * Not really much point in saying a lot here.
 * This has all the methods most closely related
 * to reading from, writing to, and updating 
 * the database.  That's all there is to it.
 * 
 * @author Bo Zimmerman
 *
 */
public interface DatabaseEngine extends CMLibrary
{
	/**
	 * An enum of all the database table types.
	 * These are the dividers by which different
	 * connections to different databases can be
	 * assigned to different tables.
	 * 
	 * @author Bo Zimmerman
	 *
	 */
	public static enum DatabaseTables
	{
		DBABILITY,
		DBCHARCLASS,
		DBRACE,
		DBPLAYERS,
		DBPLAYERDATA,
		DBMAP,
		DBSTATS,
		DBPOLLS,
		DBVFS,
		DBJOURNALS,
		DBQUEST,
		DBCLANS,
		DBBACKLOG
	}

	/**
	 * Returns the database status, formatted for html.
	 * @return the database status, formatted for html.
	 */
	public String errorStatus();

	/**
	 * Forces all existing database connections to be closed,
	 * and then re-open.
	 */
	public void resetConnections();

	/**
	 * Returns the connector object to the database, allowing
	 * SQL statements to be run.  
	 * @return the connector object to the database
	 */
	public DBConnector getConnector();

	/**
	 * "Pings" all connections to the database by issueing
	 * a "SELECT 1 FROM CMCHAR".
	 * @return the number of connections pinged
	 */
	public int pingAllConnections();

	/**
	 * "Pings" all connections to the database by issueing
	 * a "SELECT 1 FROM CMCHAR", if the connection has
	 * not seen any action in the given number of milliseconds.
	 * @param overrideTimeoutIntervalMillis the connection timeout
	 * @return the number of connections pinged
	 */
	public int pingAllConnections(final long overrideTimeoutIntervalMillis);

	/**
	 * Returns whether the database is connected.
	 * @return whether the database is connected.
	 */
	public boolean isConnected();

	/**
	 * 
	 * @param sql
	 * @return
	 * @throws CMException
	 */
	public int DBRawExecute(String sql) throws CMException;

	/**
	 * 
	 * @param sql
	 * @return
	 * @throws CMException
	 */
	public List<String[]> DBRawQuery(String sql) throws CMException;

	/**
	 * Table category: DBMAP
	 * 
	 */
	public void DBReadCatalogs();

	/**
	 * Table category: DBMAP
	 * 
	 */
	public void DBReadSpace();

	/**
	 * Table category: DBMAP
	 * 
	 * @param roomID
	 * @param thisRoom
	 * @param makeLive
	 */
	public void DBReadContent(String roomID, Room thisRoom, boolean makeLive);

	/**
	 * Table category: DBMAP
	 * 
	 * @param A
	 * @return
	 */
	public Area DBReadArea(Area A);

	/**
	 * Table category: DBMAP
	 * 
	 * @param roomID
	 * @param reportStatus
	 * @return
	 */
	public Map<String, Room> DBReadRoomData(String roomID, boolean reportStatus);

	/**
	 * Table category: DBMAP
	 * 
	 * @param room
	 * @return
	 */
	public boolean DBReReadRoomObject(Room room);

	/**
	 * Table category: DBMAP
	 * 
	 * @param roomIDtoLoad
	 * @param reportStatus
	 * @return
	 */
	public Room DBReadRoomObject(String roomIDtoLoad, boolean reportStatus);

	/**
	 * Table category: DBMAP
	 * 
	 * @param areaName
	 * @param reportStatus
	 * @return
	 */
	public Room[] DBReadRoomObjects(String areaName, boolean reportStatus);

	/**
	 * Table category: DBMAP
	 * 
	 * @param roomID
	 * @param room
	 * @param reportStatus
	 */
	public void DBReadRoomExits(String roomID, Room room, boolean reportStatus);

	/**
	 * Table category: DBMAP
	 * 
	 * @param room
	 */
	public void DBUpdateExits(Room room);

	/**
	 * Table category: DBMAP
	 * 
	 * @param roomID
	 * @param thisItem
	 */
	public void DBCreateThisItem(String roomID, Item thisItem);

	/**
	 * Table category: DBMAP
	 * 
	 * @param roomID
	 * @param thisMOB
	 */
	public void DBCreateThisMOB(String roomID, MOB thisMOB);

	/**
	 * Table category: DBMAP
	 * 
	 * @param roomID
	 * @param mobID
	 * @return
	 */
	public String DBReadRoomMOBData(String roomID, String mobID);

	/**
	 * Table category: DBMAP
	 * 
	 * @param roomID
	 * @return
	 */
	public String DBReadRoomDesc(String roomID);

	/**
	 * Table category: DBMAP
	 * 
	 * @param roomsToRead
	 */
	public void DBReadAllRooms(RoomnumberSet roomsToRead);

	/**
	 * Table category: DBMAP
	 * 
	 * @param room
	 * @param mobs
	 */
	public void DBUpdateTheseMOBs(Room room, List<MOB> mobs);

	/**
	 * Table category: DBMAP
	 * 
	 * @param room
	 * @param item
	 */
	public void DBUpdateTheseItems(Room room, List<Item> item);

	/**
	 * Table category: DBMAP
	 * 
	 * @param room
	 */
	public void DBUpdateMOBs(Room room);

	/**
	 * Table category: DBMAP
	 * 
	 * @param room
	 */
	public void DBCreateRoom(Room room);

	/**
	 * Table category: DBMAP
	 * 
	 * @param room
	 */
	public void DBUpdateRoom(Room room);
	
	/**
	 * Table category: DBMAP
	 * 
	 * @param areaName
	 * @param reportStatus
	 * @return
	 */
	public RoomnumberSet DBReadAreaRoomList(String areaName, boolean reportStatus);

	/**
	 * Table category: DBMAP
	 * 
	 * @param roomID
	 * @param mob
	 */
	public void DBUpdateMOB(String roomID, MOB mob);

	/**
	 * Table category: DBMAP
	 * 
	 * @param roomID
	 * @param item
	 */
	public void DBUpdateItem(String roomID, Item item);

	/**
	 * Table category: DBMAP
	 * 
	 * @param roomID
	 * @param mob
	 */
	public void DBDeleteMOB(String roomID, MOB mob);

	/**
	 * Table category: DBMAP
	 * 
	 * @param roomID
	 * @param item
	 */
	public void DBDeleteItem(String roomID, Item item);

	/**
	 * Table category: DBMAP
	 * 
	 * @param room
	 */
	public void DBUpdateItems(Room room);

	/**
	 * Table category: DBMAP
	 * 
	 * @param room
	 * @param oldID
	 */
	public void DBReCreate(Room room, String oldID);

	/**
	 * Table category: DBMAP
	 * 
	 * @param room
	 */
	public void DBDeleteRoom(Room room);

	/**
	 * Table category: DBMAP
	 * 
	 * @param A
	 */
	public void DBCreateArea(Area A);

	/**
	 * Table category: DBMAP
	 * 
	 * @param A
	 */
	public void DBDeleteArea(Area A);

	/**
	 * Table category: DBMAP
	 * 
	 * @param keyName
	 * @param A
	 */
	public void DBUpdateArea(String keyName,Area A);

	/**
	 * Table category: DBPLAYERS
	 * 
	 * @param topThisMany
	 * @param scanCPUPercent
	 * @return
	 */
	public List<Pair<String,Integer>>[][] DBScanPridePlayerWinners(int topThisMany, short scanCPUPercent);

	/**
	 * Table category: DBPLAYERS
	 * 
	 * @param topThisMany
	 * @param scanCPUPercent
	 * @return
	 */
	public List<Pair<String,Integer>>[][] DBScanPrideAccountWinners(int topThisMany, short scanCPUPercent);

	/**
	 * Table category: DBPLAYERS
	 * 
	 * @param mob
	 */
	public void DBUpdatePlayer(MOB mob);

	/**
	 * Table category: DBPLAYERS
	 * 
	 * @param skipNames
	 * @return
	 */
	public List<String> DBExpiredCharNameSearch(Set<String> skipNames);

	/**
	 * Table category: DBPLAYERS
	 * 
	 * @param mob
	 */
	public void DBUpdatePlayerPlayerStats(MOB mob);

	/**
	 * Table category: DBPLAYERS
	 * 
	 * @param mob
	 */
	public void DBUpdatePlayerMOBOnly(MOB mob);

	/**
	 * Table category: DBPLAYERS
	 * 
	 * @param mob
	 */
	public void DBUpdatePlayerAbilities(MOB mob);

	/**
	 * Table category: DBPLAYERS
	 * 
	 * @param mob
	 */
	public void DBUpdatePlayerItems(MOB mob);

	/**
	 * Table category: DBPLAYERS
	 * This method deletes and re-saves the non-player
	 * npc followers of the given player mob.
	 * @param mob the mob whose followers to save
	 */
	public void DBUpdateFollowers(MOB mob);

	/**
	 * Table category: DBPLAYERS
	 * 
	 * @param account
	 */
	public void DBUpdateAccount(PlayerAccount account);

	/**
	 * Table category: DBPLAYERS
	 * 
	 * @param account
	 */
	public void DBCreateAccount(PlayerAccount account);

	/**
	 * Table category: DBPLAYERS
	 * 
	 * @param account
	 */
	public void DBDeleteAccount(PlayerAccount account);

	/**
	 * Table category: DBPLAYERS
	 * 
	 * @param Login
	 * @return
	 */
	public PlayerAccount DBReadAccount(String Login);

	/**
	 * Table category: DBPLAYERS
	 * 
	 * @param name
	 * @return
	 */
	public MOB DBReadPlayer(String name);

	/**
	 * Table category: DBPLAYERS
	 * 
	 * @param mask
	 * @return
	 */
	public List<PlayerAccount> DBListAccounts(String mask);

	/**
	 * Table category: DBPLAYERS
	 * 
	 * @param oldName
	 * @param newName
	 */
	public void DBPlayerNameChange(String oldName, String newName);

	/**
	 * Table category: DBPLAYERS
	 * 
	 * @param mob
	 */
	public void DBUpdateEmail(MOB mob);

	/**
	 * Table category: DBPLAYERS
	 * 
	 * @param name
	 * @param password
	 */
	public void DBUpdatePassword(String name, String password);

	/**
	 * Table category: DBPLAYERS
	 * 
	 * @param name
	 * @return
	 */
	public String[] DBFetchEmailData(String name);

	/**
	 * Table category: DBPLAYERS
	 * 
	 * @param email
	 * @return
	 */
	public String DBPlayerEmailSearch(String email);

	/**
	 * Table category: DBPLAYERS
	 * 
	 * @return
	 */
	public List<PlayerLibrary.ThinPlayer> getExtendedUserList();

	/**
	 * Table category: DBPLAYERS
	 * 
	 * @param name
	 * @return
	 */
	public PlayerLibrary.ThinPlayer getThinUser(String name);

	/**
	 * Table category: DBPLAYERS
	 * 
	 * @return
	 */
	public List<String> getUserList();

	/**
	 * Table category: DBPLAYERS
	 * 
	 * @param mob
	 * @return
	 */
	public List<MOB> DBScanFollowers(MOB mob);

	/**
	 * Table category: DBPLAYERS
	 * 
	 * @param mob
	 * @param bringToLife
	 */
	public void DBReadFollowers(MOB mob, boolean bringToLife);

	/**
	 * Table category: DBPLAYERS
	 * 
	 * @param mob
	 * @param deleteAssets
	 */
	public void DBDeletePlayer(MOB mob, boolean deleteAssets);

	/**
	 * Table category: DBPLAYERS
	 * 
	 * @param mob
	 */
	public void DBCreateCharacter(MOB mob);

	/**
	 * Table category: DBPLAYERS
	 * 
	 * @param Login
	 * @return
	 */
	public PlayerLibrary.ThinnerPlayer DBUserSearch(String Login);

	/**
	 * Table category: DBPLAYERS
	 * 
	 * @param mob
	 * @param liegeID
	 * @return
	 */
	public List<PlayerLibrary.ThinPlayer> vassals(MOB mob, String liegeID);

	/**
	 * Table category: DBPLAYERS
	 * 
	 * @param deityID
	 * @return
	 */
	public List<PlayerLibrary.ThinPlayer> worshippers(String deityID);

	/**
	 * Table category: DBPLAYERS
	 * 
	 * @param tattoo
	 * @return
	 */
	public Tattoo parseTattoo(String tattoo);

	/**
	 * Table category: DBQUEST
	 * 
	 * @param quests
	 */
	public void DBUpdateQuests(List<Quest> quests);

	/**
	 * Table category: DBQUEST
	 * 
	 * @param Q
	 */
	public void DBUpdateQuest(Quest Q);

	/**
	 * Table category: DBQUEST
	 * 
	 * @param myHost
	 */
	public void DBReadQuests(MudHost myHost);

	/**
	 * Table category: DBCLANS
	 * 
	 * @param clan
	 * @return
	 */
	public List<MemberRecord> DBClanMembers(String clan);

	/**
	 * Table category: DBCLANS
	 * 
	 * @param clan
	 * @param name
	 * @return
	 */
	public MemberRecord DBGetClanMember(String clan, String name);

	/**
	 * Table category: DBCLANS
	 * 
	 * @param clan
	 * @param name
	 * @param adjMobKills
	 * @param adjPlayerKills
	 */
	public void DBUpdateClanKills(String clan, String name, int adjMobKills, int adjPlayerKills);

	/**
	 * Table category: DBCLANS
	 * 
	 * @param name
	 * @param clan
	 * @param role
	 */
	public void DBUpdateClanMembership(String name, String clan, int role);

	/**
	 * Table category: DBCLANS
	 * 
	 */
	public void DBReadAllClans();

	/**
	 * Table category: DBCLANS
	 * 
	 * @param C
	 */
	public void DBUpdateClan(Clan C);

	/**
	 * Table category: DBCLANS
	 * 
	 * @param C
	 */
	public void DBUpdateClanItems(Clan C);

	/**
	 * Table category: DBCLANS
	 * 
	 * @param C
	 */
	public void DBDeleteClan(Clan C);

	/**
	 * Table category: DBCLANS
	 * 
	 * @param C
	 */
	public void DBCreateClan(Clan C);

	/**
	 * Table category: DBJOURNALS
	 * 
	 * @return
	 */
	public List<String> DBReadJournals();

	/**
	 * Table category: DBJOURNALS
	 * 
	 * @param Journal
	 * @param stats
	 */
	public void DBUpdateJournalStats(String Journal, JournalsLibrary.JournalSummaryStats stats);

	/**
	 * Table category: DBJOURNALS
	 * 
	 * @param Journal
	 * @param entry
	 */
	public void DBUpdateJournal(String Journal, JournalEntry entry);

	/**
	 * Table category: DBJOURNALS
	 * 
	 * @param Journal
	 * @param searchStr
	 * @return
	 */
	public Vector<JournalEntry> DBSearchAllJournalEntries(String Journal, String searchStr);

	/**
	 * Table category: DBJOURNALS
	 * 
	 * @param stats
	 */
	public void DBReadJournalSummaryStats(JournalsLibrary.JournalSummaryStats stats);

	/**
	 * Table category: DBJOURNALS
	 * 
	 * @param key
	 * @param numReplies
	 */
	public void DBUpdateMessageReplies(String key, int numReplies);

	/**
	 * Table category: DBJOURNALS
	 * 
	 * @param Journal
	 * @param Key
	 * @return
	 */
	public JournalEntry DBReadJournalEntry(String Journal, String Key);

	/**
	 * Table category: DBJOURNALS
	 * 
	 * @param Journal
	 * @param parent
	 * @param searchStr
	 * @param newerDate
	 * @param limit
	 * @return
	 */
	public Vector<JournalEntry> DBReadJournalPageMsgs(String Journal, String parent, String searchStr, long newerDate, int limit);

	/**
	 * Table category: DBJOURNALS
	 * 
	 * @param Journal
	 * @return
	 */
	public List<JournalEntry> DBReadJournalMsgs(String Journal);

	/**
	 * Table category: DBJOURNALS
	 * 
	 * @param Journal
	 * @param to
	 * @param olderDate
	 * @return
	 */
	public Vector<JournalEntry> DBReadJournalMsgsNewerThan(String Journal, String to, long olderDate);

	/**
	 * Table category: DBJOURNALS
	 * 
	 * @param Journal
	 * @param from
	 * @param to
	 * @return
	 */
	public int DBCountJournal(String Journal, String from, String to);

	/**
	 * Table category: DBJOURNALS
	 * 
	 * @param Journal
	 * @param entry
	 */
	public void DBWriteJournal(String Journal, JournalEntry entry);

	/**
	 * Table category: DBJOURNALS
	 * 
	 * @param Journal
	 * @param from
	 * @param to
	 * @param subject
	 * @param message
	 */
	public void DBWriteJournal(String Journal, String from, String to, String subject, String message);

	/**
	 * Table category: DBJOURNALS
	 * 
	 * @param MailBox
	 * @param journalSource
	 * @param from
	 * @param to
	 * @param subject
	 * @param message
	 */
	public void DBWriteJournalEmail(String MailBox, String journalSource, String from, String to, String subject, String message);

	/**
	 * Table category: DBJOURNALS
	 * 
	 * @param Journal
	 * @param key
	 * @param from
	 * @param to
	 * @param subject
	 * @param message
	 */
	public void DBWriteJournalReply(String Journal, String key, String from, String to, String subject, String message);

	/**
	 * Table category: DBJOURNALS
	 * 
	 * @param Journal
	 * @param journalSource
	 * @param from
	 * @param to
	 * @param parentKey
	 * @param subject
	 * @param message
	 */
	public void DBWriteJournalChild(String Journal, String journalSource, String from, String to, 
									String parentKey, String subject, String message);

	/**
	 * Table category: DBJOURNALS
	 * 
	 * @param Journal
	 * @param msgKeyOrNull
	 */
	public void DBDeleteJournal(String Journal, String msgKeyOrNull);

	/**
	 * Table category: DBJOURNALS
	 * 
	 * @param possibleName
	 * @return
	 */
	public String DBGetRealJournalName(String possibleName);

	/**
	 * Table category: DBJOURNALS
	 * 
	 * @param Journal
	 * @param to
	 * @param olderTime
	 * @return
	 */
	public long[] DBJournalLatestDateNewerThan(String Journal, String to, long olderTime);

	/**
	 * Table category: DBJOURNALS
	 * 
	 * @param name
	 */
	public void DBDeletePlayerJournals(String name);

	/**
	 * Table category: DBJOURNALS
	 * 
	 * @param key
	 * @param subject
	 * @param msg
	 * @param newAttributes
	 */
	public void DBUpdateJournal(String key, String subject, String msg, long newAttributes);

	/**
	 * Table category: DBJOURNALS
	 * 
	 * @param key
	 * @param views
	 */
	public void DBViewJournalMessage(String key, int views);

	/**
	 * Table category: DBJOURNALS
	 * 
	 * @param key
	 */
	public void DBTouchJournalMessage(String key);

	/**
	 * Table category: DBJOURNALS
	 * 
	 * @param key
	 * @param newDate
	 */
	public void DBTouchJournalMessage(String key, long newDate);

	/**
	 * Table category: DBPLAYERDATA
	 * 
	 * @param playerID
	 * @return
	 */
	public List<PlayerData> DBReadAllPlayerData(String playerID);

	/**
	 * Table category: DBPLAYERDATA
	 * 
	 * @param playerID
	 * @param section
	 * @return
	 */
	public List<PlayerData> DBReadData(String playerID, String section);

	/**
	 * Table category: DBPLAYERDATA
	 * 
	 * @param playerID
	 * @param section
	 * @return
	 */
	public int DBCountData(String playerID, String section);

	/**
	 * Table category: DBPLAYERDATA
	 * 
	 * @param playerID
	 * @param section
	 * @param key
	 * @return
	 */
	public List<PlayerData> DBReadData(String playerID, String section, String key);

	/**
	 * Table category: DBPLAYERDATA
	 * 
	 * @param section
	 * @param keyMask
	 * @return
	 */
	public List<PlayerData> DBReadDataKey(String section, String keyMask);

	/**
	 * Table category: DBPLAYERDATA
	 * 
	 * @param key
	 * @return
	 */
	public List<PlayerData> DBReadDataKey(String key);

	/**
	 * Table category: DBPLAYERDATA
	 * 
	 * @param section
	 * @return
	 */
	public List<PlayerData> DBReadData(String section);

	/**
	 * Table category: DBPLAYERDATA
	 * 
	 * @param player
	 * @param sections
	 * @return
	 */
	public List<PlayerData> DBReadData(String player, List<String> sections);

	/**
	 * Table category: DBPLAYERDATA
	 * 
	 * @param name
	 */
	public void DBDeletePlayerData(String name);

	/**
	 * Table category: DBPLAYERDATA
	 * 
	 * @param playerID
	 * @param section
	 */
	public void DBDeleteData(String playerID, String section);

	/**
	 * Table category: DBPLAYERDATA
	 * 
	 * @param playerID
	 * @param section
	 * @param key
	 */
	public void DBDeleteData(String playerID, String section, String key);

	/**
	 * Table category: DBPLAYERDATA
	 * 
	 * @param key
	 * @param xml
	 */
	public void DBUpdateData(String key, String xml);

	/**
	 * Table category: DBPLAYERDATA
	 * 
	 * @param name
	 * @param section
	 * @param key
	 * @param xml
	 */
	public void DBReCreateData(String name, String section, String key, String xml);

	/**
	 * Table category: DBPLAYERDATA
	 * 
	 * @param section
	 */
	public void DBDeleteData(String section);

	/**
	 * Table category: DBPLAYERDATA
	 * 
	 * @param player
	 * @param section
	 * @param key
	 * @param data
	 */
	public void DBCreateData(String player, String section, String key, String data);

	/**
	 * Table category: DBPLAYERDATA
	 * 
	 */
	public void DBReadArtifacts();

	/**
	 * Table category: DBPLAYERDATA
	 * 
	 * @return
	 */
	public PlayerData createPlayerData();

	/**
	 * Table category: DBRACE
	 * 
	 * @return
	 */
	public List<AckRecord> DBReadRaces();

	/**
	 * Table category: DBRACE
	 * 
	 * @param raceID
	 */
	public void DBDeleteRace(String raceID);

	/**
	 * Table category: DBRACE
	 * 
	 * @param raceID
	 * @param data
	 */
	public void DBCreateRace(String raceID,String data);

	/**
	 * Table category: DBCHARCLASS
	 * 
	 * @return
	 */
	public List<AckRecord> DBReadClasses();

	/**
	 * Table category: DBCHARCLASS
	 * 
	 * @param classID
	 */
	public void DBDeleteClass(String classID);

	/**
	 * Table category: DBCHARCLASS
	 * 
	 * @param classID
	 * @param data
	 */
	public void DBCreateClass(String classID,String data);

	/**
	 * Table category: DBABILITY
	 * 
	 * @return
	 */
	public List<AckRecord> DBReadAbilities();

	/**
	 * 
	 * @param classID
	 */
	public void DBDeleteAbility(String classID);

	/**
	 * Table category: DBABILITY
	 * 
	 * @param classID
	 * @param typeClass
	 * @param data
	 */
	public void DBCreateAbility(String classID, String typeClass, String data);

	/**
	 * Table category: DBSTATS
	 * 
	 * @param startTime
	 * @return
	 */
	public Object DBReadStat(long startTime);

	/**
	 * Table category: DBSTATS
	 * 
	 * @param startTime
	 */
	public void DBDeleteStat(long startTime);

	/**
	 * Table category: DBSTATS
	 * 
	 * @param startTime
	 * @param endTime
	 * @param data
	 * @return
	 */
	public boolean DBCreateStat(long startTime,long endTime,String data);

	/**
	 * Table category: DBSTATS
	 * 
	 * @param startTime
	 * @param data
	 * @return
	 */
	public boolean DBUpdateStat(long startTime, String data);

	/**
	 * Table category: DBSTATS
	 * 
	 * @param startTime
	 * @return
	 */
	public List<CoffeeTableRow> DBReadStats(long startTime);

	/**
	 * Table category: DBPOLLS
	 * 
	 * @param name
	 * @param player
	 * @param subject
	 * @param description
	 * @param optionXML
	 * @param flag
	 * @param qualZapper
	 * @param results
	 * @param expiration
	 */
	public void DBCreatePoll(String name, String player, String subject, String description, String optionXML, 
							 int flag, String qualZapper, String results, long expiration);

	/**
	 * Table category: DBPOLLS
	 * 
	 * @param OldName
	 * @param name
	 * @param player
	 * @param subject
	 * @param description
	 * @param optionXML
	 * @param flag
	 * @param qualZapper
	 * @param results
	 * @param expiration
	 */
	public void DBUpdatePoll(String OldName, String name, String player, String subject, String description, 
							 String optionXML, int flag, String qualZapper, String results, long expiration);

	/**
	 * Table category: DBPOLLS
	 * 
	 * @param name
	 * @param results
	 */
	public void DBUpdatePollResults(String name, String results);

	/**
	 * Table category: DBPOLLS
	 * 
	 * @param name
	 */
	public void DBDeletePoll(String name);

	/**
	 * Table category: DBPOLLS
	 * 
	 * @return
	 */
	public List<PollData> DBReadPollList();

	/**
	 * Table category: DBPOLLS
	 * 
	 * @param name
	 * @return
	 */
	public PollData DBReadPoll(String name);

	/**
	 * Table category: DBVFS
	 * 
	 * @return
	 */
	public CMFile.CMVFSDir DBReadVFSDirectory();

	/**
	 * Table category: DBVFS
	 * 
	 * @param filename
	 * @return
	 */
	public CMFile.CMVFSFile DBReadVFSFile(String filename);

	/**
	 * Table category: DBVFS
	 * 
	 * @param filename
	 * @param bits
	 * @param creator
	 * @param updateTime
	 * @param data
	 */
	public void DBCreateVFSFile(String filename, int bits, String creator, long updateTime, Object data);

	/**
	 * Table category: DBVFS
	 * 
	 * @param filename
	 * @param bits
	 * @param creator
	 * @param updateTime
	 * @param data
	 */
	public void DBUpSertVFSFile(String filename, int bits, String creator, long updateTime, Object data);

	/**
	 * Table category: DBVFS
	 * 
	 * @param filename
	 */
	public void DBDeleteVFSFile(String filename);

	/**
	 * Table category: DBBACKLOG
	 * 
	 * @param channelName
	 * @param entry
	 */
	public void addBackLogEntry(String channelName, final String entry);

	/**
	 * Table category: DBBACKLOG
	 * 
	 * @param channelName
	 * @param newestToSkip
	 * @param numToReturn
	 * @return
	 */
	public List<Pair<String,Long>> getBackLogEntries(String channelName, final int newestToSkip, final int numToReturn);

	/**
	 * Table category: DBBACKLOG
	 * 
	 * @param channels
	 * @param maxMessages
	 * @param oldestTime
	 */
	public void trimBackLogEntries(final String[] channels, final int maxMessages, final long oldestTime);

	/**
	 * Table category: DBPLAYERDATA
	 * 
	 * @author Bo Zimmerman
	 *
	 */
	public static interface PlayerData
	{

		/**
		 * 
		 * @return
		 */
		public String who();

		/**
		 * 
		 * @param who
		 * @return
		 */
		public PlayerData who(String who);

		/**
		 * 
		 * @return
		 */
		public String section();

		/**
		 * 
		 * @param section
		 * @return
		 */
		public PlayerData section(String section);

		/**
		 * 
		 * @return
		 */
		public String key();

		/**
		 * 
		 * @param key
		 * @return
		 */
		public PlayerData key(String key);

		/**
		 * 
		 * @return
		 */
		public String xml();

		/**
		 * 
		 * @param xml
		 * @return
		 */
		public PlayerData xml(String xml);
	}

	/**
	 * Table category: DBPOLLS
	 * 
	 * @author Bo Zimmerman
	 *
	 */
	public static interface PollData
	{

		/**
		 * 
		 * @return
		 */
		public String name();

		/**
		 * 
		 * @return
		 */
		public long flag();

		/**
		 * 
		 * @return
		 */
		public String byName();

		/**
		 * 
		 * @return
		 */
		public String subject();

		/**
		 * 
		 * @return
		 */
		public String description();

		/**
		 * 
		 * @return
		 */
		public String options();

		/**
		 * 
		 * @return
		 */
		public String qual();

		/**
		 * 
		 * @return
		 */
		public String results();

		/**
		 * 
		 * @return
		 */
		public long expiration();
	}

	/**
	 * Table category: DBRACE, DBCHARCLASS, DBABILITY
	 * 
	 * @author Bo Zimmerman
	 *
	 */
	public static interface AckRecord
	{

		/**
		 * 
		 * @return
		 */
		public String ID();

		/**
		 * 
		 * @return
		 */
		public String data();

		/**
		 * 
		 * @return
		 */
		public String typeClass();
	}

}
