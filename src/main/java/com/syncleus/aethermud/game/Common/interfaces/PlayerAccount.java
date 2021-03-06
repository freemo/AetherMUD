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
package com.syncleus.aethermud.game.Common.interfaces;

import com.syncleus.aethermud.game.Libraries.interfaces.PlayerLibrary;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMParms;
import com.syncleus.aethermud.game.core.interfaces.Modifiable;
import com.syncleus.aethermud.game.core.interfaces.Tattooable;

import java.util.Enumeration;
import java.util.Vector;

/**
 * An interface for a base player account.  If this system is enabled, this
 * represents essentially a "container" for various characters, who
 * share a login and potentially an expiration date.
 */
public interface PlayerAccount extends CMCommon, AccountStats, Modifiable, Tattooable {
    /**
     * Return an enumeration of the fully loaded players
     * that belong to this account.
     * @return an enumeration of player mob objects
     */
    public Enumeration<MOB> getLoadPlayers();

    /**
     * Return an enumeration of the semi-loaded players
     * that belong to this account.
     * @return an enumeration of thinplayer objects
     */
    public Enumeration<PlayerLibrary.ThinPlayer> getThinPlayers();

    /**
     * Returns the number of players this account currently
     * has listed.
     * @return the number of players
     */
    public int numPlayers();

    /**
     * Return an enumeration of the players names
     * that belong to this account.
     * @return an enumeration of player names
     */
    public Enumeration<String> getPlayers();

    /**
     * Adds a new player to this account.
     * @param mob the new player to add.
     */
    public void addNewPlayer(MOB mob);

    /**
     * Removes a player from this account.
     * This is typically a precursor to deleting the player.
     * @param mob the player to delete.
     */
    public void delPlayer(MOB mob);

    /**
     * Removes a player of this name from this account.
     * @param name the name of the player to remove.
     */
    public void delPlayer(String name);

    /**
     * Retrieves a fake account mob, for forum and
     * other access systems not directly relayed to gameplay.
     * @return mob the fake player.
     */
    public MOB getAccountMob();

    /**
     * Returns the real name if the player is on this account
     * @param name the name look for check
     * @return real name if it exists and null otherwise
     */
    public String findPlayer(String name);

    /**
     * Returns this accounts name
     * @return this accounts name
     */
    public String getAccountName();

    /**
     * Sets this accounts unique name
     * @param name the accounts name
     */
    public void setAccountName(String name);

    /**
     * Sets the names of all the players that belong to this account
     * @param names the names of the players
     */
    public void setPlayerNames(Vector<String> names);

    /**
     * Checks whether the given string flag is set for this account.
     * @see com.syncleus.aethermud.game.Common.interfaces.PlayerAccount#setFlag(AccountFlag, boolean)
     * @param flag the flag name
     * @return true if it is set, false if not
     */
    public boolean isSet(AccountFlag flag);

    /**
     * Sets or unsets an account-wide flag.
     * @see com.syncleus.aethermud.game.Common.interfaces.PlayerAccount#isSet(AccountFlag)
     * @param flag the flag name
     * @param setOrUnset true to set it, false to unset
     */
    public void setFlag(AccountFlag flag, boolean setOrUnset);

    /**
     * Returns the number of bonus characters online available to
     * this account.
     *
     * @see PlayerAccount#setBonusCharsOnlineLimit(int)
     *
     * @return the number of bonus chars online
     */
    public int getBonusCharsOnlineLimit();

    /**
     * Sets the number of bonus characters online available to
     * this account.
     *
     * @see PlayerAccount#getBonusCharsOnlineLimit()
     *
     * @param bonus the number of bonus chars online
     */
    public void setBonusCharsOnlineLimit(int bonus);

    /**
     * Returns the number of bonus characters available to
     * this account.
     *
     * @see PlayerAccount#setBonusCharsLimit(int)
     *
     * @return the number of bonus chars
     */
    public int getBonusCharsLimit();

    /**
     * Sets the number of bonus characters available to
     * this account.
     *
     * @see PlayerAccount#getBonusCharsLimit()
     *
     * @param bonus the number of bonus chars
     */
    public void setBonusCharsLimit(int bonus);

    /**
     * Populates this account object with all the data
     * from the given one, replacing any existing internal
     * data.
     * @param otherAccount the data to copy from.
     */
    public void copyInto(PlayerAccount otherAccount);

    /**
     * Various account-level flags
     * @author Bo Zimmerman
     *
     */
    public enum AccountFlag {
        /** Constant for account flags that overrides number of characters limitation */
        NUMCHARSOVERRIDE,
        /** Constant for account flags that overrides account expiration */
        NOEXPIRE,
        /** Constant for account flags that overrides account expiration */
        CANEXPORT,
        /** Constant for account flags that overrides account expiration */
        MAXCONNSOVERRIDE,
        /** Constant for account flags that overrides account expiration */
        ANSI,
        /** Constant for account flags that overrides account expiration */
        ACCOUNTMENUSOFF;

        /**
         * Returns a comma-delimited list of strings representing the accountflag values
         * @return a comma-delimited list of strings representing the accountflag values
         */
        public static String getListString() {
            return CMParms.toListString(AccountFlag.values());
        }
    }
}
