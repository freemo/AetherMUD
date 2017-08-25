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
package com.planet_ink.game.Items.interfaces;

import com.planet_ink.game.MOBS.interfaces.MOB;

import java.util.List;


/**
 * A perfume item is one that can be "worn", and when worn, causes
 * smell-message emotes to issue forth for others in the same room.
 * @author Bo Zimmerman
 */
public interface Perfume {
    /**
     * Retrieves a list of the possible smell emotes when worn.
     * @return a list of the possible smell emotes when worn.
     */
    public List<String> getSmellEmotes();

    /**
     * Gets the list of possible smell emotes when worn as a
     * semicolon delimited list of strings.
     * @see Perfume#setSmellList(String)
     * @return the list of possible smell emotes
     */
    public String getSmellList();

    /**
     * Sets the list of possible smell emotes when worn as a
     * semicolon delimited list of strings.
     * @see Perfume#getSmellList()
     * @param list the list of possible smell emotes
     */
    public void setSmellList(String list);

    /**
     * Causes this perfume to be "worn" on the given mob.
     * They will, for a time, begin emotting the smells.
     * @param mob the mob to wear the perfume.
     */
    public void wearIfAble(MOB mob);
}