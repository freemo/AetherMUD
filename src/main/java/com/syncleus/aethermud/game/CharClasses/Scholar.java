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
package com.syncleus.aethermud.game.CharClasses;

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Areas.interfaces.Area;
import com.syncleus.aethermud.game.CharClasses.interfaces.CharClass;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Common.interfaces.CharStats;
import com.syncleus.aethermud.game.Items.interfaces.Item;
import com.syncleus.aethermud.game.Items.interfaces.Weapon;
import com.syncleus.aethermud.game.Libraries.interfaces.ExpertiseLibrary;
import com.syncleus.aethermud.game.Libraries.interfaces.TimeManager;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.*;
import com.syncleus.aethermud.game.core.collections.Pair;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Tickable;

import java.util.*;


public class Scholar extends StdCharClass {
    private final static String localizedStaticName = CMLib.lang().L("Scholar");
    private final Set<Integer> disallowedWeapons = buildDisallowedWeaponClasses();
    private final String[] raceRequiredList = new String[]{"All"};
    @SuppressWarnings("unchecked")
    private final Pair<String, Integer>[] minimumStatRequirements = new Pair[]
        {
            new Pair<String, Integer>("Intelligence", Integer.valueOf(9)),
            new Pair<String, Integer>("Wisdom", Integer.valueOf(6))
        };

    public Scholar() {
        super();
        maxStatAdj[CharStats.STAT_WISDOM] = 6;
        maxStatAdj[CharStats.STAT_INTELLIGENCE] = 6;
    }

    @Override
    public String ID() {
        return "Scholar";
    }

    @Override
    public String name() {
        return localizedStaticName;
    }

    @Override
    public String baseClass() {
        return "Commoner";
    }

    @Override
    public int getBonusPracLevel() {
        return 1;
    }

    @Override
    public int getBonusAttackLevel() {
        return -1;
    }

    @Override
    public int getAttackAttribute() {
        return CharStats.STAT_INTELLIGENCE;
    }

    @Override
    public int getLevelsPerBonusDamage() {
        return 50;
    }

    @Override
    public int maxLanguages() {
        return CMProps.getIntVar(CMProps.Int.MAXLANGUAGES) + 3;
    }

    @Override
    public String getHitPointsFormula() {
        return "((@x6<@x7)/9)+(1*(1?3))";
    }

    @Override
    public String getManaFormula() {
        return "((@x4<@x5)/9)+(1*(1?2))";
    }

    @Override
    public int allowedArmorLevel() {
        return CharClass.ARMOR_CLOTH;
    }

    @Override
    public int allowedWeaponLevel() {
        return CharClass.WEAPONS_STAFFONLY;
    }

    @Override
    protected Set<Integer> disallowedWeaponClasses(MOB mob) {
        return disallowedWeapons;
    }

    @Override
    public int availabilityCode() {
        return Area.THEME_FANTASY | Area.THEME_SKILLONLYMASK;
    }

    @Override
    public void initializeClass() {
        super.initializeClass();
        CMLib.ableMapper().addCharAbilityMapping(ID(), 1, "Skill_Write", true);
        CMLib.ableMapper().addCharAbilityMapping(ID(), 1, "Skill_Swim", false);
        CMLib.ableMapper().addCharAbilityMapping(ID(), 1, "Herbology", true);
        CMLib.ableMapper().addCharAbilityMapping(ID(), 1, "Skill_Recall", 100, true);
        CMLib.ableMapper().addCharAbilityMapping(ID(), 1, "Studying", true);

        CMLib.ableMapper().addCharAbilityMapping(ID(), 2, "Tagging", true);

        CMLib.ableMapper().addCharAbilityMapping(ID(), 3, "PaperMaking", true);
        CMLib.ableMapper().addCharAbilityMapping(ID(), 3, "Skill_CombatLog", false);

        CMLib.ableMapper().addCharAbilityMapping(ID(), 4, "Thief_Mark", false);
        CMLib.ableMapper().addCharAbilityMapping(ID(), 4, "Organizing", false);

        CMLib.ableMapper().addCharAbilityMapping(ID(), 5, "Dissertating", true);
        CMLib.ableMapper().addCharAbilityMapping(ID(), 5, "Skill_WandUse", false);
        CMLib.ableMapper().addCharAbilityMapping(ID(), 5, "Fighter_SmokeSignals", false);

/*
5	Dissertating  (G), Skill_WandUse(Q), Smoke Signals (Q)
6	Copywriting (G), Morse Code (Q)
7	Edit (G), Identify Poison (Q), Druid_KnowPlants
8	Skill_SeaMapping (G), RevealText (Q)
9	Bookcopying (G), Semaphore (Q), Wilderness Lore (Q)
10	Speculating (G), Subtitling (Q), Siegecraft (Q)
11	Thief_Lore (G), Skillcraft (Q), InvisibleInk (Q)
12	Studying (G), Honorary Degree: Commonerness (Q)
13	Cataloging (G), Songcraft (Q), Skill_Map (Q)
14	Honorary Degree: Fighterness (Q), Thief_Observere (Q), Thief_AnalyzeMark (Q)
15	Spell_DetectMagic (Q)  Spellcraft (Q), Plant Lore (Q)
16	 Instructing (G), Honorary Degree: Bardness (Q), Taxidermy (Q)
17	 Shush (Q)  Prayercraft (Q), Appraise (Q)
18	Recipecopying (G) Honorary Degree: Thiefness (Q),
19	ScrollScribing (Q)  Chantcraft (Q)
20	Publish (Q) Honorary Degree: Mageness (Q)
21	Revise (Q), Encrypting (Q)
22	Surveying (G), Honorary Degree: Druidness (Q)
23	Lecturing (G)
24	Honorary Degree: Clericness (Q), Thief_Comprehension
25	Enrolling (G)
26
27
28
29
30	 Guildmaster (G)
 */

//		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Costuming",false,CMParms.parseSemicolons("Tailoring",true));
    }

    @Override
    public boolean okMessage(final Environmental myHost, final CMMsg msg) {
        // no xp from combat
        if ((msg.sourceMinor() == CMMsg.TYP_EXPCHANGE)
            && (msg.source() == myHost)
            && (msg.target() instanceof MOB)
            && (((MOB) msg.target()).amDead() || (((MOB) msg.target()).curState().getHitPoints() <= 0))
            && (msg.value() > 0)) {
            msg.setValue(0);
        }
        return super.okMessage(myHost, msg);
    }

    @Override
    public boolean canBeADivider(MOB killer, MOB killed, MOB mob, Set<MOB> followers) {
        return false;
    }

    @Override
    public boolean canBeABenificiary(MOB killer, MOB killed, MOB mob, Set<MOB> followers) {
        return false;
    }

    @Override
    public int addedExpertise(final MOB host, final ExpertiseLibrary.Flag expertiseCode, final String abilityID) {
        if ((expertiseCode == ExpertiseLibrary.Flag.XPCOST) && (abilityID.equals("ScrollScribing")))
            return 15;
        return 0;
    }

    @Override
    public void executeMsg(Environmental myHost, CMMsg msg) {
        if (msg.source() == myHost) {
            if (((msg.targetMinor() == CMMsg.TYP_WRITE)
                || (msg.targetMinor() == CMMsg.TYP_WROTE))
                && (msg.target() instanceof Item)
                && (msg.targetMessage() != null)
                && (msg.targetMessage().length() > 0)) {
                if ((msg.tool() instanceof Ability)
                    && (msg.targetMinor() == CMMsg.TYP_WROTE)
                    && (msg.tool().ID().equals("Skill_Map") || msg.tool().ID().equals("Thief_TreasureMap") || msg.tool().ID().equals("Skill_SeaMapping")))
                    CMLib.leveler().postExperience(msg.source(), null, null, 10, false);
                else if ((msg.tool() instanceof Ability)
                    && (msg.targetMinor() == CMMsg.TYP_WROTE)
                    && (msg.tool().ID().equals("Skill_Dissertation")))
                    CMLib.leveler().postExperience(msg.source(), null, null, 25, false);
                else {
                    final String msgStr = msg.targetMessage().trim();
                    int numChars = msgStr.length() - CMStrings.countChars(msgStr, ' ');
                    if (numChars > 10) {
                        final Map<String, Object> persMap = Resources.getPersonalMap(myHost, true);
                        if (persMap != null) {
                            int xp = numChars / 10;
                            long[] xpTrap = (long[]) persMap.get("SCHOLAR_WRITEXP");
                            if (xpTrap == null) {
                                xpTrap = new long[2];
                                persMap.put("SCHOLAR_WRITEXP", xpTrap);
                            }
                            if (System.currentTimeMillis() > xpTrap[1]) {
                                xpTrap[0] = 0;
                                xpTrap[1] = System.currentTimeMillis() + TimeManager.MILI_HOUR;
                            }
                            if (xpTrap[0] < 100) {
                                if (100 - xpTrap[0] < xp)
                                    xp = (int) (100 - xpTrap[0]);
                                xpTrap[0] += xp;
                                CMLib.leveler().postExperience(msg.source(), null, null, xp, false);
                            }
                        }
                    }
                }
            }
        } else if ((msg.tool() instanceof Ability)
            && (myHost instanceof MOB)
            && (((MOB) myHost).getVictim() != msg.source())
            && (msg.source().getVictim() != myHost)
            && (CMLib.dice().rollPercentage() < 25)
            && (msg.source().fetchAbility(msg.tool().ID()) != null)
            && (((MOB) myHost).fetchAbility(msg.tool().ID()) != null)
            && (((MOB) myHost).getGroupMembers(new TreeSet<MOB>()).contains(msg.source()))) {
            final Ability A = (Ability) msg.tool();
            if ((A != null) && (A.isSavable()))
                A.helpProficiency(msg.source(), 0);
        }
        super.executeMsg(myHost, msg);
    }

    @Override
    public boolean tick(Tickable ticking, int tickID) {
        if ((tickID == Tickable.TICKID_MOB) && (ticking instanceof MOB)) {
            final MOB mob = (MOB) ticking;
            if (ID().equals(mob.charStats().getCurrentClass().ID())) {
            }
        }
        return super.tick(ticking, tickID);
    }

    @Override
    public String[] getRequiredRaceList() {
        return raceRequiredList;
    }

    @Override
    public Pair<String, Integer>[] getMinimumStatRequirements() {
        return minimumStatRequirements;
    }

    @Override
    public List<Item> outfit(MOB myChar) {
        if (outfitChoices == null) {
            outfitChoices = new Vector<Item>();

            final Weapon w = CMClass.getWeapon("Staff");
            if (w == null)
                return new Vector<Item>();
            outfitChoices.add(w);

            final Item I = CMClass.getBasicItem("GenJournal");
            I.setName(L("Scholar`s Logbook"));
            I.setDisplayText(L("A Scholar`s Logbook has been left here."));
            outfitChoices.add(I);
        }
        return outfitChoices;
    }

    @Override
    public String getOtherLimitsDesc() {
        return L("Earns no combat experience.");
    }

    @Override
    public String getOtherBonusDesc() {
        return L("Earn experience from teaching skills, making maps, writing books and journals. Gives bonus profficiency gains for group members.");
    }
}
