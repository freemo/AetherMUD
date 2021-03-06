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

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;


/**
 * A scroll is a piece of paper upon which magical spells are written.
 * Scrolls can be used to learn new spells, cast spells directly off
 * them, and copy known spells onto.
 * @author Bo Zimmerman
 */
public interface Scroll extends Item, SpellHolder {
    /**
     * Causes the given mob to cast the given spell/effect
     * through this scroll.  This will remove the spell
     * from the scroll.
     * @param A the spell on the scroll to cast
     * @param mob the person reading the spell on the scroll.
     * @return true if the spell was cast, false otherwise
     */
    public boolean useTheScroll(Ability A, MOB mob);

    /**
     * Checks whether the given mob was the last one to
     * decipher the runes on this scroll through the Read
     * Magic ability, allowing them to freely read the
     * contents of the scroll in the future.
     * @see Scroll#setReadableScrollBy(String)
     * @param name the name of the player to check
     * @return true if the player has already read, false otherwise
     */
    public boolean isReadableScrollBy(String name);

    /**
     * Sets the given mob as the last one to
     * decipher the runes on this scroll through the Read
     * Magic ability, allowing them to freely read the
     * contents of the scroll in the future.
     * @see Scroll#isReadableScrollBy(String)
     * @param name the name of the player to set
     */
    public void setReadableScrollBy(String name);

    /**
     * Reads the given spell off this scroll for the
     * given mob, if they are able.  Any errors will
     * be messaged directly to the mob.
     * @param mob the mob who is trying to read
     * @param spellName the spell the mob is trying to read
     */
    public void readIfAble(MOB mob, String spellName);
}
