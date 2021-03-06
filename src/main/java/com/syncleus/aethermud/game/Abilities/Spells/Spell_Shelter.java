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
package com.syncleus.aethermud.game.Abilities.Spells;

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Items.interfaces.Item;
import com.syncleus.aethermud.game.Locales.interfaces.Room;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.ItemPossessor.Expire;
import com.syncleus.aethermud.game.core.interfaces.ItemPossessor.Move;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


public class Spell_Shelter extends Spell {

    private final static String localizedName = CMLib.lang().L("Shelter");
    private final static String localizedStaticDisplay = CMLib.lang().L("(In a shelter)");
    public Room previousLocation = null;
    public Room shelter = null;

    @Override
    public String ID() {
        return "Spell_Shelter";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    public String displayText() {
        return localizedStaticDisplay;
    }

    @Override
    protected int canAffectCode() {
        return CAN_MOBS;
    }

    @Override
    protected int canTargetCode() {
        return 0;
    }

    @Override
    public int enchantQuality() {
        return Ability.QUALITY_INDIFFERENT;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_INDIFFERENT;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_SPELL | Ability.DOMAIN_CONJURATION;
    }

    public Room getPreviousLocation(MOB mob) {
        if ((previousLocation == null) || (previousLocation.amDestroyed())) {
            if (text().length() > 0)
                previousLocation = CMLib.map().getRoom(text());
            while ((previousLocation == null) || (previousLocation.amDestroyed()) || (!CMLib.flags().canAccess(mob, previousLocation)))
                previousLocation = CMLib.map().getRandomRoom();
        }
        return previousLocation;
    }

    @Override
    public void unInvoke() {
        if (!(affected instanceof MOB))
            return;
        final MOB M = (MOB) affected;

        if (canBeUninvoked()) {
            Room shelter = this.shelter;
            if (shelter == null)
                shelter = M.location();
            if (shelter != null) {
                this.shelter = null;
                Room backToRoom = M.getStartRoom();
                int i = 0;
                final LinkedList<MOB> mobs = new LinkedList<MOB>();
                for (final Enumeration<MOB> m = shelter.inhabitants(); m.hasMoreElements(); )
                    mobs.add(m.nextElement());
                for (final MOB mob : mobs) {
                    if (mob == null)
                        break;
                    mob.tell(L("You return to your previous location."));

                    final CMMsg enterMsg = CMClass.getMsg(mob, previousLocation, null, CMMsg.MSG_ENTER, null, CMMsg.MSG_ENTER, null, CMMsg.MSG_ENTER, L("<S-NAME> appears out of nowhere!"));
                    backToRoom = getPreviousLocation(mob);
                    if (backToRoom == null)
                        backToRoom = mob.getStartRoom();
                    backToRoom.bringMobHere(mob, false);
                    backToRoom.send(mob, enterMsg);
                    CMLib.commands().postLook(mob, true);
                }
                final LinkedList<Item> items = new LinkedList<Item>();
                for (final Enumeration<Item> e = shelter.items(); e.hasMoreElements(); )
                    items.add(e.nextElement());
                for (final Item I : items) {
                    if (I.container() == null)
                        backToRoom.moveItemTo(I, Expire.Player_Drop, Move.Followers);
                }
                i = 0;
                while (i < shelter.numItems()) {
                    final Item I = shelter.getItem(i);
                    backToRoom.moveItemTo(I, Expire.Player_Drop, Move.Followers);
                    if (shelter.isContent(I))
                        i++;
                }
                this.shelter = null;
                previousLocation = null;
            }
        }
        super.unInvoke();
    }

    @Override
    public boolean okMessage(Environmental host, CMMsg msg) {
        if (((msg.sourceMinor() == CMMsg.TYP_QUIT)
            || (msg.sourceMinor() == CMMsg.TYP_SHUTDOWN)
            || ((msg.targetMinor() == CMMsg.TYP_EXPIRE) && (msg.target() == shelter))
            || (msg.sourceMinor() == CMMsg.TYP_DEATH)
            || (msg.sourceMinor() == CMMsg.TYP_ROOMRESET))
            && (shelter != null)
            && (shelter.isInhabitant(msg.source()))) {
            getPreviousLocation(msg.source()).bringMobHere(msg.source(), false);
            unInvoke();
        }
        return super.okMessage(host, msg);
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        if (mob.fetchEffect(ID()) != null) {
            mob.fetchEffect(ID()).unInvoke();
            return false;
        }

        final boolean success = proficiencyCheck(mob, 0, auto);
        if (success) {
            final CMMsg msg = CMClass.getMsg(mob, null, this, somanticCastCode(mob, null, auto), auto ? "" : L("^S<S-NAME> wave(s) <S-HIS-HER> arms, speak(s), and suddenly vanish(es)!^?"));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                final Set<MOB> h = properTargets(mob, givenTarget, false);
                if (h == null)
                    return false;

                final Room thisRoom = mob.location();
                previousLocation = thisRoom;
                shelter = CMClass.getLocale("MagicShelter");
                final Room newRoom = shelter;
                shelter.setArea(mob.location().getArea());
                miscText = CMLib.map().getExtendedRoomID(mob.location());
                for (final Object element : h) {
                    final MOB follower = (MOB) element;
                    final CMMsg enterMsg = CMClass.getMsg(follower, newRoom, null, CMMsg.MSG_ENTER, null, CMMsg.MSG_ENTER, null, CMMsg.MSG_ENTER, L("<S-NAME> appears out of nowhere."));
                    final CMMsg leaveMsg = CMClass.getMsg(follower, thisRoom, this, verbalCastCode(mob, newRoom, auto), L("<S-NAME> disappear(s) into oblivion."));
                    if (thisRoom.okMessage(follower, leaveMsg) && newRoom.okMessage(follower, enterMsg)) {
                        if (follower.isInCombat()) {
                            CMLib.commands().postFlee(follower, ("NOWHERE"));
                            follower.makePeace(false);
                        }
                        thisRoom.send(follower, leaveMsg);
                        newRoom.bringMobHere(follower, false);
                        thisRoom.delInhabitant(follower);
                        newRoom.send(follower, enterMsg);
                        follower.tell(L("\n\r\n\r"));
                        CMLib.commands().postLook(follower, true);
                        if (follower == mob)
                            beneficialAffect(mob, mob, asLevel, 999999);
                    }
                }
            }
        } else
            beneficialVisualFizzle(mob, null, L("<S-NAME> wave(s) <S-HIS-HER> arms and and speak(s), but nothing happens."));

        return success;
    }
}
