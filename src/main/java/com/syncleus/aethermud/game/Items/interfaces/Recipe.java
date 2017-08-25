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
package com.syncleus.aethermud.game.Items.interfaces;

/**
 * A Recipe is a special readable item that, merely by having, adds
 * to the recipes available to construction-type common skills, such
 * as tailoring, weaponsmithing, etc.
 * @author Bo Zimmerman
 *
 */
public interface Recipe extends Item {
    /**
     * Gets the Ability ID of the skill that
     * this Recipe item adds a new recipe to.
     * @see Recipe#setCommonSkillID(String)
     * @return the Ability ID this enhances
     */
    public String getCommonSkillID();

    /**
     * Sets the Ability ID of the skill that
     * this Recipe item adds a new recipe to.
     * @see Recipe#getCommonSkillID()
     * @param ID the Ability ID this enhances
     */
    public void setCommonSkillID(String ID);

    /**
     * Gets the number of pages in this recipe
     * book, denoting how many recipes it has, or at
     * least how many it will hold.
     * @see Recipe#setTotalRecipePages(int)
     * @return the number of pages in this recipe
     */
    public int getTotalRecipePages();

    /**
     * Sets the number of pages in this recipe
     * book, denoting how many recipes it has, or at
     * least how many it will hold.
     * @see Recipe#getTotalRecipePages()
     * @param numRemaining the number of pages in this recipe
     */
    public void setTotalRecipePages(int numRemaining);

    /**
     * Gets all the recipes written on this Recipe item,
     * one on each line/page, and each line encoded
     * according to the specific common skill it
     * applies to.
     * @see Recipe#setRecipeCodeLines(String[])
     * @return all the recipes written on this Recipe item
     */
    public String[] getRecipeCodeLines();

    /**
     * Sets all the recipes written on this Recipe item,
     * one on each line/page, and each line encoded
     * according to the specific common skill it
     * applies to.
     * @see Recipe#getRecipeCodeLines()
     * @param lines all the recipes written on this Recipe item
     */
    public void setRecipeCodeLines(String[] lines);
}

