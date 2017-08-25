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
package com.planet_ink.game.core.interfaces;

/**
 * Interface for objects which represents property purchasable by players.  May
 * be found implemented by Abilities which are placed as effects on the room  objects
 * for sale, or implemented as Items representing the sellable title.
 * @author Bo Zimmerman
 */
public interface PrivateProperty extends Environmental {
    /**
     * The value of the property in base currency values
     * @return the price of the property
     */
    public int getPrice();

    /**
     * set the value of the property in base currency values
     * @param price the price of the property
     */
    public void setPrice(int price);

    /**
     * Get the owner of the property, usually a clan name or a player name.
     * @return the name of the owner of the property
     */
    public String getOwnerName();

    /**
     * Set the owner of the property, usually a clan name or a player name.
     * @param owner the name of the owner of the property
     */
    public void setOwnerName(String owner);

    /**
     * Get the actual clan or mob owner of the property, or null if it can not.
     * @return the owner of the property
     */
    public CMObject getOwnerObject();

    /**
     * Returns a unique id for this particular title and the rooms is represents, even if the contents change.
     * @return a unique id
     */
    public String getTitleID();
}