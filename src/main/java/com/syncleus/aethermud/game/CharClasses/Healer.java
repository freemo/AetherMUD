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
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMParms;
import com.syncleus.aethermud.game.core.CMath;
import com.syncleus.aethermud.game.core.collections.Pair;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Tickable;

import java.util.List;
import java.util.Set;
import java.util.Vector;


public class Healer extends Cleric {
    private final static String localizedStaticName = CMLib.lang().L("Healer");
    private final Set<Integer> disallowedWeapons = buildDisallowedWeaponClasses();
    private final String[] raceRequiredList = new String[]{
        "Human", "Humanoid", "Dwarf", "Elf", "HalfElf", "Fairy-kin", "Svirfneblin",
        "LizardMan", "Aarakocran", "Merfolk", "Faerie", "Elf-kin", "-Duergar", "-Drow"
    };
    @SuppressWarnings("unchecked")
    private final Pair<String, Integer>[] minimumStatRequirements = new Pair[]{
        new Pair<String, Integer>("Wisdom", Integer.valueOf(9)),
        new Pair<String, Integer>("Charisma", Integer.valueOf(9))
    };
    protected volatile long auraCheckTime = System.currentTimeMillis();

    public Healer() {
        super();
        maxStatAdj[CharStats.STAT_WISDOM] = 4;
        maxStatAdj[CharStats.STAT_CHARISMA] = 4;
    }

    @Override
    public String ID() {
        return "Healer";
    }

    @Override
    public String name() {
        return localizedStaticName;
    }

    @Override
    public String baseClass() {
        return "Cleric";
    }

    @Override
    public int getAttackAttribute() {
        return CharStats.STAT_WISDOM;
    }

    @Override
    public int allowedWeaponLevel() {
        return CharClass.WEAPONS_GOODCLERIC;
    }

    @Override
    protected Set<Integer> disallowedWeaponClasses(MOB mob) {
        return disallowedWeapons;
    }

    @Override
    protected int alwaysFlunksThisQuality() {
        return 0;
    }

    @Override
    public void initializeClass() {
        super.initializeClass();
        CMLib.ableMapper().addCharAbilityMapping(ID(), 1, "Skill_Write", 50, true);
        CMLib.ableMapper().addCharAbilityMapping(ID(), 1, "Skill_Recall", 100, true);
        CMLib.ableMapper().addCharAbilityMapping(ID(), 1, "Skill_Revoke", true);
        CMLib.ableMapper().addCharAbilityMapping(ID(), 1, "Skill_WandUse", false);
        CMLib.ableMapper().addCharAbilityMapping(ID(), 1, "Skill_Swim", true);
        CMLib.ableMapper().addCharAbilityMapping(ID(), 1, "Skill_Convert", 50, true);
        CMLib.ableMapper().addCharAbilityMapping(ID(), 1, "Skill_TurnUndead", 0, true);
        CMLib.ableMapper().addCharAbilityMapping(ID(), 1, "Specialization_Natural", true);
        CMLib.ableMapper().addCharAbilityMapping(ID(), 1, "Prayer_Marry", false);
        CMLib.ableMapper().addCharAbilityMapping(ID(), 1, "Prayer_Annul", false);

        CMLib.ableMapper().addCharAbilityMapping(ID(), 1, "Prayer_CureLight", true);
        CMLib.ableMapper().addCharAbilityMapping(ID(), 1, "Prayer_RestoreSmell", false);

        CMLib.ableMapper().addCharAbilityMapping(ID(), 2, "Prayer_SenseEvil", false);
        CMLib.ableMapper().addCharAbilityMapping(ID(), 2, "Prayer_InfuseHoliness", false);

        CMLib.ableMapper().addCharAbilityMapping(ID(), 3, "Prayer_Sacrifice", true);
        CMLib.ableMapper().addCharAbilityMapping(ID(), 3, "Prayer_RemoveDeathMark", false);

        CMLib.ableMapper().addCharAbilityMapping(ID(), 4, "Prayer_ProtEvil", false);
        CMLib.ableMapper().addCharAbilityMapping(ID(), 4, "Prayer_CureFatigue", false);

        CMLib.ableMapper().addCharAbilityMapping(ID(), 5, "Prayer_CureDeafness", true);
        CMLib.ableMapper().addCharAbilityMapping(ID(), 5, "Prayer_Fidelity", false);

        CMLib.ableMapper().addCharAbilityMapping(ID(), 6, "Prayer_CureSerious", true, CMParms.parseSemicolons("Prayer_CureLight", true));
        CMLib.ableMapper().addCharAbilityMapping(ID(), 6, "Prayer_SenseDisease", false);

        CMLib.ableMapper().addCharAbilityMapping(ID(), 7, "Prayer_Bless", false);
        CMLib.ableMapper().addCharAbilityMapping(ID(), 7, "Prayer_BrighteningAura", false);

        CMLib.ableMapper().addCharAbilityMapping(ID(), 8, "Prayer_Freedom", true);
        CMLib.ableMapper().addCharAbilityMapping(ID(), 8, "Prayer_Forgive", false);
        CMLib.ableMapper().addCharAbilityMapping(ID(), 8, "Prayer_RemoveParalysis", false);

        CMLib.ableMapper().addCharAbilityMapping(ID(), 9, "Prayer_DispelEvil", false);
        CMLib.ableMapper().addCharAbilityMapping(ID(), 9, "Prayer_GodLight", false);

        CMLib.ableMapper().addCharAbilityMapping(ID(), 10, "Prayer_RestoreVoice", true);
        CMLib.ableMapper().addCharAbilityMapping(ID(), 10, "Prayer_CureVampirism", false);

        CMLib.ableMapper().addCharAbilityMapping(ID(), 11, "Prayer_RemovePoison", false);
        CMLib.ableMapper().addCharAbilityMapping(ID(), 11, "Prayer_Benediction", false);

        CMLib.ableMapper().addCharAbilityMapping(ID(), 12, "Prayer_CureDisease", true);
        CMLib.ableMapper().addCharAbilityMapping(ID(), 12, "Prayer_CureExhaustion", false);

        CMLib.ableMapper().addCharAbilityMapping(ID(), 13, "Prayer_ProtectHealth", false);
        CMLib.ableMapper().addCharAbilityMapping(ID(), 13, "Prayer_MassFreedom", true, CMParms.parseSemicolons("Prayer_Freedom", true));

        CMLib.ableMapper().addCharAbilityMapping(ID(), 14, "Prayer_CureCritical", true, CMParms.parseSemicolons("Prayer_CureSerious", true));
        CMLib.ableMapper().addCharAbilityMapping(ID(), 14, "Prayer_AuraHeal", false);

        CMLib.ableMapper().addCharAbilityMapping(ID(), 15, "Prayer_HolyAura", false, CMParms.parseSemicolons("Prayer_Bless", true));
        CMLib.ableMapper().addCharAbilityMapping(ID(), 15, "Prayer_HolyShield", true);

        CMLib.ableMapper().addCharAbilityMapping(ID(), 16, "Prayer_Calm", true);
        CMLib.ableMapper().addCharAbilityMapping(ID(), 16, "Prayer_CureCannibalism", false);

        CMLib.ableMapper().addCharAbilityMapping(ID(), 17, "Prayer_CureBlindness", true);
        CMLib.ableMapper().addCharAbilityMapping(ID(), 17, "Prayer_Invigorate", false);

        CMLib.ableMapper().addCharAbilityMapping(ID(), 18, "Prayer_DispelUndead", false);
        CMLib.ableMapper().addCharAbilityMapping(ID(), 18, "Prayer_BlessedHearth", false);

        CMLib.ableMapper().addCharAbilityMapping(ID(), 19, "Prayer_Godstrike", false);
        CMLib.ableMapper().addCharAbilityMapping(ID(), 19, "Prayer_DeathsDoor", false);

        CMLib.ableMapper().addCharAbilityMapping(ID(), 20, "Prayer_PeaceRitual", false);
        CMLib.ableMapper().addCharAbilityMapping(ID(), 20, "Skill_AttackHalf", true);

        CMLib.ableMapper().addCharAbilityMapping(ID(), 21, "Prayer_Heal", true, CMParms.parseSemicolons("Prayer_CureCritical", true));
        CMLib.ableMapper().addCharAbilityMapping(ID(), 21, "Prayer_Atonement", false);

        CMLib.ableMapper().addCharAbilityMapping(ID(), 22, "Prayer_BlessItem", false, CMParms.parseSemicolons("Prayer_Bless", true));
        CMLib.ableMapper().addCharAbilityMapping(ID(), 22, "Prayer_ConsecrateLand", false);

        CMLib.ableMapper().addCharAbilityMapping(ID(), 23, "Prayer_MassHeal", true, CMParms.parseSemicolons("Prayer_Heal", true));
        CMLib.ableMapper().addCharAbilityMapping(ID(), 23, "Prayer_MassCureDisease", false, CMParms.parseSemicolons("Prayer_CureDisease", true));

        CMLib.ableMapper().addCharAbilityMapping(ID(), 24, "Prayer_HolyWord", false, CMParms.parseSemicolons("Prayer_HolyAura", true));
        CMLib.ableMapper().addCharAbilityMapping(ID(), 24, "Prayer_DivineResistance", false);

        CMLib.ableMapper().addCharAbilityMapping(ID(), 25, "Prayer_DivineConstitution", false, CMParms.parseSemicolons("Prayer_HolyAura;Prayer_DeathsDoor;Prayer_Heal", true));

        CMLib.ableMapper().addCharAbilityMapping(ID(), 30, "Prayer_TrueResurrection", true);
        // level 30 == healing aura
    }

    @Override
    public int availabilityCode() {
        return Area.THEME_FANTASY;
    }

    @Override
    public int classLevelModifier(MOB myChar, Ability skill, int level) {
        if ((myChar.charStats().getCurrentClass() == this)
            && (skill.ID().equals("Prayer_CureDisease") || skill.ID().equals("Prayer_MassCureDisease"))) {
            return level + level;
        }
        return level;
    }

    @Override
    public boolean tick(Tickable ticking, int tickID) {
        if (!(ticking instanceof MOB))
            return super.tick(ticking, tickID);
        final MOB myChar = (MOB) ticking;
        if (tickID != Tickable.TICKID_MOB)
            return super.tick(ticking, tickID);
        if ((System.currentTimeMillis() - auraCheckTime) > 2 * 60 * 1000) {
            if ((System.currentTimeMillis() - auraCheckTime) > 3 * 60 * 1000) {
                auraCheckTime = System.currentTimeMillis();
            }
            affectHealingAura(myChar);
        }
        return super.tick(myChar, tickID);
    }

    public void affectHealingAura(MOB myChar) {
        Ability A = myChar.fetchEffect("Prayer_HealingAura");
        if ((myChar.charStats().getClassLevel(this) >= 30) && (CMLib.flags().isGood(myChar))) {
            if (A == null) {
                A = CMClass.getAbility("Prayer_HealingAura");
                if (A != null) {
                    A.setInvoker(myChar);
                    myChar.addNonUninvokableEffect(A);
                }
            }
        } else if (A != null) {
            myChar.delEffect(A);
            A.destroy();
        }
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
    public String getOtherBonusDesc() {
        return L("All healing prayers give bonus healing.  Attains healing aura after 30th level.");
    }

    @Override
    public String getOtherLimitsDesc() {
        return L("Always fumbles evil prayers.  Qualifies and receives good prayers.  Using non-aligned prayers introduces failure chance.");
    }

    @Override
    public void executeMsg(final Environmental myHost, final CMMsg msg) {
        super.executeMsg(myHost, msg);
        if (!(myHost instanceof MOB))
            return;
        final MOB myChar = (MOB) myHost;
        if (msg.amISource(myChar)) {
            if (msg.sourceMinor() == CMMsg.TYP_LIFE)
                affectHealingAura(myChar);
            if ((!myChar.isMonster())
                && (msg.targetMinor() == CMMsg.TYP_HEALING)
                && (msg.tool() instanceof Ability)
                && (CMLib.ableMapper().getQualifyingLevel(ID(), true, msg.tool().ID()) > 0)
                && (myChar.isMine(msg.tool()))
                && ((((Ability) msg.tool()).classificationCode() & Ability.ALL_ACODES) == Ability.ACODE_PRAYER)
                && (msg.value() > 0))
                msg.setValue((int) Math.round(CMath.mul(msg.value(), 2.0)));
        }
    }

    @Override
    public List<Item> outfit(MOB myChar) {
        if (outfitChoices == null) {
            final Weapon w = CMClass.getWeapon("SmallMace");
            if (w == null)
                return new Vector<Item>();
            outfitChoices = new Vector<Item>();
            outfitChoices.add(w);
        }
        return outfitChoices;
    }

}
