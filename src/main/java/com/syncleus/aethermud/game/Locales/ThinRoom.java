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
package com.syncleus.aethermud.game.Locales;

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Areas.interfaces.Area;
import com.syncleus.aethermud.game.Behaviors.interfaces.Behavior;
import com.syncleus.aethermud.game.Common.interfaces.*;
import com.syncleus.aethermud.game.Exits.interfaces.Exit;
import com.syncleus.aethermud.game.Items.interfaces.Item;
import com.syncleus.aethermud.game.Items.interfaces.RawMaterial;
import com.syncleus.aethermud.game.Libraries.interfaces.WorldMap;
import com.syncleus.aethermud.game.Locales.interfaces.GridLocale;
import com.syncleus.aethermud.game.Locales.interfaces.Room;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.*;
import com.syncleus.aethermud.game.core.collections.EmptyEnumeration;
import com.syncleus.aethermud.game.core.collections.Filterer;
import com.syncleus.aethermud.game.core.collections.ReadOnlyVector;
import com.syncleus.aethermud.game.core.interfaces.*;

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;


public class ThinRoom implements Room {
    protected static final Vector<Integer> empty = new ReadOnlyVector<Integer>(1);
    protected static final Exit[] exits = new Exit[Directions.NUM_DIRECTIONS()];
    protected static final Room[] rooms = new Room[Directions.NUM_DIRECTIONS()];
    protected static final PhyStats phyStats = (PhyStats) CMClass.getCommon("DefaultPhyStats");
    private static final Enumeration<MOB> emptyMobs = new EmptyEnumeration<MOB>();
    private static final Enumeration<Item> emptyItems = new EmptyEnumeration<Item>();
    private static final Enumeration<Ability> emptyEffects = new EmptyEnumeration<Ability>();
    private static final Enumeration<Behavior> emptyBehaviors = new EmptyEnumeration<Behavior>();
    private static final Enumeration<ScriptingEngine> emptyScripts = new EmptyEnumeration<ScriptingEngine>();
    private static final String[] CODES = {"CLASS"};
    protected String roomID = "";
    protected Area myArea = null;
    private boolean recurse = false;
    private boolean amDestroyed = false;

    @Override
    public String ID() {
        return "ThinRoom";
    }

    @Override
    public String name() {
        return "A Thin Room";
    }

    @Override
    public String Name() {
        return name();
    }

    @Override
    public String description() {
        return "";
    }

    @Override
    public String displayText() {
        return "";
    }

    @Override
    public String displayText(MOB mob) {
        return "";
    }

    @Override
    public String description(MOB mob) {
        return "";
    }

    @Override
    public int maxRange() {
        return 5;
    }

    @Override
    public int minRange() {
        return 0;
    }

    @Override
    public boolean isSavable() {
        return false;
    }

    @Override
    public void setSavable(boolean truefalse) {
    }

    @Override
    public int getTickStatus() {
        return Tickable.STATUS_NOT;
    }

    @Override
    public String roomID() {
        return roomID;
    }

    @Override
    public void setRoomID(String newRoomID) {
        roomID = newRoomID;
    }

    @Override
    public void initializeClass() {
    }

    @Override
    public String getContextName(Environmental E) {
        return E == null ? "nothing" : E.name();
    }

    @Override
    public int getAtmosphereCode() {
        return ATMOSPHERE_INHERIT;
    }

    @Override
    public int getAtmosphere() {
        return (myArea != null) ? myArea.getAtmosphere() : RawMaterial.RESOURCE_AIR;
    }

    @Override
    public void setAtmosphere(int resourceCode) {
    }

    @Override
    public int domainType() {
        return Room.DOMAIN_OUTDOORS_CITY;
    }

    @Override
    public int getClimateTypeCode() {
        return CLIMASK_INHERIT;
    }

    @Override
    public int getClimateType() {
        return (myArea == null) ? CLIMASK_NORMAL : myArea.getClimateType();
    }

    @Override
    public void setClimateType(int climate) {
    }

    @Override
    public int myResource() {
        return -1;
    }

    @Override
    public void setResource(int resourceCode) {
    }

    @Override
    public List<Integer> resourceChoices() {
        return empty;
    }

    @Override
    public void toggleMobility(boolean onoff) {
    }

    @Override
    public boolean getMobility() {
        return true;
    }

    @Override
    public boolean isHere(Environmental E) {
        return false;
    }

    @Override
    public void setRawExit(int direction, Exit E) {
        if (E != null)
            exits[direction] = E;
        else
            exits[direction] = null;
    }

    @Override
    public Room prepareRoomInDir(Room R, int direction) {
        if (R == null)
            return null;
        if ((roomID == null) || (roomID.length() == 0) || (recurse))
            return null;
        recurse = true;
        Room myR = null;
        synchronized (("SYNC" + roomID).intern()) {
            myR = CMLib.map().getRoom(roomID);
            if (myR == null) {
                myR = CMLib.database().DBReadRoom(roomID, false);
                if (myR != null) {
                    CMLib.database().DBReadRoomExits(roomID, myR, false);
                    CMLib.database().DBReadContent(roomID, myR, true);
                    myR.getArea().fillInAreaRoom(R);
                    if (CMath.bset(myR.getArea().flags(), Area.FLAG_THIN))
                        myR.setExpirationDate(System.currentTimeMillis() + WorldMap.ROOM_EXPIRATION_MILLIS);
                }
            }
        }
        if ((myR != null)
            && (direction >= 0)
            && (direction < Directions.NUM_DIRECTIONS())
            && (R.rawDoors()[direction] == this))
            R.rawDoors()[direction] = myR;
        recurse = false;
        if (myR instanceof ThinRoom)
            return myR;
        if (myR != null)
            return myR.prepareRoomInDir(R, direction);
        return null;
    }

    @Override
    public void startItemRejuv() {
    }

    @Override
    public void recoverRoomStats() {
    }

    @Override
    public long expirationDate() {
        return 0;
    }

    @Override
    public void setExpirationDate(long time) {
    }

    @Override
    public void clearSky() {
    }

    @Override
    public void giveASky(int depth) {
    }

    @Override
    public List<Room> getSky() {
        List<Room> skys = new Vector<Room>(0);
        return skys;
    }

    @Override
    public Area getArea() {
        return myArea;
    }

    @Override
    public void setArea(Area newArea) {
        myArea = newArea;
    }

    @Override
    public GridLocale getGridParent() {
        return null;
    }

    @Override
    public void setGridParent(GridLocale room) {
    }

    @Override
    public Room[] rawDoors() {
        return rooms;
    }

    @Override
    public Exit getRawExit(int dir) {
        if (dir < exits.length)
            return exits[dir];
        return null;
    }

    @Override
    public Exit getReverseExit(int direction) {
        return null;
    }

    @Override
    public Exit getPairedExit(int direction) {
        return null;
    }

    @Override
    public Room getRoomInDir(int direction) {
        return null;
    }

    @Override
    public Exit getExitInDir(int direction) {
        return null;
    }

    @Override
    public int pointsPerMove() {
        return 0;
    }

    @Override
    public int thirstPerRound() {
        return 0;
    }

    @Override
    public void send(MOB source, CMMsg msg) {
    }

    @Override
    public void sendOthers(MOB source, CMMsg msg) {
    }

    @Override
    public void showHappens(int allCode, String allMessage) {
    }

    @Override
    public void showHappens(int allCode, Environmental like, String allMessage) {
    }

    @Override
    public boolean show(MOB source,
                        Environmental target,
                        int allCode,
                        String allMessage) {
        return true;
    }

    @Override
    public boolean show(MOB source,
                        Environmental target,
                        Environmental tool,
                        int allCode,
                        String allMessage) {
        return true;
    }

    @Override
    public boolean show(MOB source,
                        Environmental target,
                        Environmental tool,
                        int srcCode,
                        int tarCode,
                        int othCode,
                        String allMessage) {
        return true;
    }

    @Override
    public boolean show(MOB source,
                        Environmental target,
                        Environmental tool,
                        int srcCode,
                        String srcMessage,
                        int tarCode,
                        String tarMessage,
                        int othCode,
                        String othMessage) {
        return true;
    }

    @Override
    public boolean show(MOB source,
                        Environmental target,
                        Environmental tool,
                        int allCode,
                        String srcMessage,
                        String tarMessage,
                        String othMessage) {
        return true;
    }

    @Override
    public boolean showOthers(MOB source,
                              Environmental target,
                              int allCode,
                              String allMessage) {
        return true;
    }

    @Override
    public boolean showSource(MOB source,
                              Environmental target,
                              int allCode,
                              String allMessage) {
        return true;
    }

    @Override
    public boolean showOthers(MOB source,
                              Environmental target,
                              Environmental tool,
                              int allCode,
                              String allMessage) {
        return true;
    }

    @Override
    public boolean showSource(MOB source,
                              Environmental target,
                              Environmental tool,
                              int allCode,
                              String allMessage) {
        return true;
    }

    @Override
    public void eachInhabitant(final EachApplicable<MOB> applier) {
    }

    @Override
    public MOB fetchInhabitant(String inhabitantID) {
        return null;
    }

    @Override
    public MOB fetchRandomInhabitant() {
        return null;
    }

    @Override
    public List<MOB> fetchInhabitants(String inhabitantID) {
        return new Vector<MOB>(1);
    }

    @Override
    public void addInhabitant(MOB mob) {
    }

    @Override
    public void delInhabitant(MOB mob) {
    }

    @Override
    public void delAllInhabitants(boolean destroy) {
    }

    @Override
    public int numInhabitants() {
        return 0;
    }

    @Override
    public Enumeration<MOB> inhabitants() {
        return emptyMobs;
    }

    @Override
    public boolean isInhabitant(MOB mob) {
        return false;
    }

    @Override
    public MOB fetchInhabitant(int i) {
        return null;
    }

    @Override
    public int numPCInhabitants() {
        return 0;
    }

    public MOB fetchPCInhabitant(int i) {
        return null;
    }

    @Override
    public void bringMobHere(MOB mob, boolean andFollowers) {
    }

    @Override
    public void setName(String newName) {
    }

    @Override
    public void setDescription(String newDescription) {
    }

    @Override
    public void setDisplayText(String newDisplayText) {
    }

    @Override
    public String image() {
        return "";
    }

    @Override
    public String rawImage() {
        return "";
    }

    @Override
    public void setImage(String newImage) {
    }

    @Override
    public String name(MOB viewerMob) {
        return name();
    }

    @Override
    public void addItem(Item item) {
    }

    @Override
    public void addItem(Item item, Expire expire) {
    }

    @Override
    public void delItem(Item item) {
    }

    @Override
    public void delAllItems(boolean destroy) {
    }

    @Override
    public int numItems() {
        return 0;
    }

    @Override
    public boolean isContent(Item item) {
        return false;
    }

    @Override
    public Item findItem(Item goodLocation, String itemID) {
        return null;
    }

    @Override
    public Item getItem(int i) {
        return null;
    }

    @Override
    public void eachItem(final EachApplicable<Item> applier) {
    }

    @Override
    public Item getRandomItem() {
        return null;
    }

    @Override
    public Enumeration<Item> items() {
        return emptyItems;
    }

    public Item getItem(String s) {
        return null;
    }

    public Item getItem(Item goodLocation, String s) {
        return null;
    }

    @Override
    public Item findItem(String itemID) {
        return null;
    }

    @Override
    public void moveItemTo(Item item, Expire expire, Move... moveFlags) {
    }

    @Override
    public void moveItemTo(Item container) {
    }

    @Override
    public List<Item> findItems(String itemID) {
        return new Vector<Item>(1);
    }

    @Override
    public List<Item> findItems(Item goodLocation, String itemID) {
        return new Vector<Item>(1);
    }

    @Override
    public Exit fetchExit(String thingName) {
        return null;
    }

    @Override
    public PhysicalAgent fetchFromRoomFavorExits(String thingName) {
        return null;
    }

    @Override
    public PhysicalAgent fetchFromRoomFavorItems(Item goodLocation, String thingName) {
        return null;
    }

    @Override
    public PhysicalAgent fetchFromMOBRoomItemExit(MOB mob, Item goodLocation, String thingName, Filterer<Environmental> filter) {
        return null;
    }

    @Override
    public PhysicalAgent fetchFromRoomFavorMOBs(Item goodLocation, String thingName) {
        return null;
    }

    @Override
    public PhysicalAgent fetchFromMOBRoomFavorsItems(MOB mob, Item goodLocation, String thingName, Filterer<Environmental> filter) {
        return null;
    }

    @Override
    public PhysicalAgent fetchFromMOBRoomFavorsMOBs(MOB mob, Item goodLocation, String thingName, Filterer<Environmental> filter) {
        return null;
    }

    @Override
    public void addEffect(Ability to) {
    }

    @Override
    public void addNonUninvokableEffect(Ability to) {
    }

    @Override
    public void delEffect(Ability to) {
    }

    @Override
    public void delAllEffects(boolean unInvoke) {
    }

    @Override
    public void eachEffect(final EachApplicable<Ability> applier) {
    }

    @Override
    public int numEffects() {
        return 0;
    }

    @Override
    public Enumeration<Ability> effects() {
        return emptyEffects;
    }

    @Override
    public Ability fetchEffect(int index) {
        return null;
    }

    @Override
    public Ability fetchEffect(String ID) {
        return null;
    }

    @Override
    public void addBehavior(Behavior to) {
    }

    @Override
    public void delBehavior(Behavior to) {
    }

    @Override
    public void delAllBehaviors() {
    }

    @Override
    public int numBehaviors() {
        return 0;
    }

    @Override
    public Enumeration<Behavior> behaviors() {
        return emptyBehaviors;
    }

    @Override
    public Behavior fetchBehavior(int index) {
        return null;
    }

    @Override
    public void eachBehavior(final EachApplicable<Behavior> applier) {
    }

    @Override
    public Behavior fetchBehavior(String ID) {
        return null;
    }

    @Override
    public void addScript(ScriptingEngine S) {
    }

    @Override
    public void delScript(ScriptingEngine S) {
    }

    @Override
    public void delAllScripts() {
    }

    @Override
    public int numScripts() {
        return 0;
    }

    @Override
    public Enumeration<ScriptingEngine> scripts() {
        return emptyScripts;
    }

    @Override
    public ScriptingEngine fetchScript(int x) {
        return null;
    }

    @Override
    public void eachScript(final EachApplicable<ScriptingEngine> applier) {
    }

    @Override
    public boolean isGeneric() {
        return false;
    }

    @Override
    public int getSaveStatIndex() {
        return getStatCodes().length;
    }

    @Override
    public String[] getStatCodes() {
        return CODES;
    }

    @Override
    public String L(final String str, final String... xs) {
        return CMLib.lang().fullSessionTranslation(str, xs);
    }

    @Override
    public boolean isStat(String code) {
        return CMParms.indexOf(getStatCodes(), code.toUpperCase().trim()) >= 0;
    }

    protected int getCodeNum(String code) {
        for (int i = 0; i < CODES.length; i++) {
            if (code.equalsIgnoreCase(CODES[i]))
                return i;
        }
        return -1;
    }

    @Override
    public String getStat(String code) {
        switch (getCodeNum(code)) {
            case 0:
                return ID();
        }
        return "";
    }

    @Override
    public void setStat(String code, String val) {
        switch (getCodeNum(code)) {
            case 0:
                return;
        }
    }

    @Override
    public boolean sameAs(Environmental E) {
        return E == this;
    }

    @Override
    public CMObject copyOf() {
        try {
            final ThinRoom E = (ThinRoom) this.clone();
            //CMClass.bumpCounter(E,CMClass.CMObjectType.LOCALE);//removed for mem & perf
            return E;

        } catch (final CloneNotSupportedException e) {
            return this.newInstance();
        }
    }

    @Override
    public void destroy() {
        amDestroyed = true;
    }

    @Override
    public boolean amDestroyed() {
        return amDestroyed;
    }

    @Override
    public PhyStats phyStats() {
        return phyStats;
    }

    @Override
    public PhyStats basePhyStats() {
        return phyStats;
    }

    // protected void finalize(){
    // CMClass.unbumpCounter(this,CMClass.CMObjectType.ABILITY); }//removed for
    // mem & perf

    @Override
    public void recoverPhyStats() {
    }

    @Override
    public void setBasePhyStats(PhyStats newStats) {
    }

    @Override
    public CMObject newInstance() {
        try {
            return this.getClass().newInstance();
        } catch (final Exception e) {
            Log.errOut(ID(), e);
        }
        return new ThinRoom();
    }

    @Override
    public int compareTo(CMObject o) {
        return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
    }

    @Override
    public void setMiscText(String newMiscText) {
    }

    @Override
    public String text() {
        return "";
    }

    @Override
    public String miscTextFormat() {
        return CMParms.FORMAT_UNDEFINED;
    }

    @Override
    public void affectPhyStats(Physical affected, PhyStats affectableStats) {
    }

    @Override
    public void affectCharStats(MOB affectedMob, CharStats affectableStats) {
    }

    @Override
    public void affectCharState(MOB affectedMob, CharState affectableMaxState) {
    }

    @Override
    public void executeMsg(final Environmental myHost, final CMMsg msg) {
        return;
    }

    @Override
    public boolean okMessage(final Environmental myHost, final CMMsg msg) {
        return true;
    }

    @Override
    public boolean tick(Tickable ticking, int tickID) {
        return true;
    }

    @Override
    public int getCombatTurnMobIndex() {
        return 0;
    }

    @Override
    public void setCombatTurnMobIndex(final int index) {
    }
}
