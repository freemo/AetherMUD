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
package com.syncleus.aethermud.game.Races;

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Areas.interfaces.Area;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Common.interfaces.CharStats;
import com.syncleus.aethermud.game.Common.interfaces.PhyStats;
import com.syncleus.aethermud.game.Items.interfaces.*;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMath;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.List;
import java.util.Vector;


public class Pixie extends SmallElfKin {
    private final static String localizedStaticName = CMLib.lang().L("Pixie");
    private final static String localizedStaticRacialCat = CMLib.lang().L("Fairy-kin");
    //                                     an ey ea he ne ar ha to le fo no gi mo wa ta wi
    private static final int[] parts = {0, 2, 2, 1, 1, 2, 2, 1, 2, 2, 1, 0, 1, 1, 0, 2};
    protected static Vector<RawMaterial> resources = new Vector<RawMaterial>();
    private final String[] racialAbilityNames = {"WingFlying"};
    private final int[] racialAbilityLevels = {1};
    private final int[] racialAbilityProficiencies = {100};
    private final boolean[] racialAbilityQuals = {false};
    private final String[] racialAbilityParms = {""};
    private final String[] culturalAbilityNames = {"Fey", "Foraging", "Spell_ImprovedInvisibility"};
    private final int[] culturalAbilityProficiencies = {100, 50, 100};

    public Pixie() {
        super();
        super.naturalAbilImmunities.add("Disease_PoisonIvy");
    }

    @Override
    public String ID() {
        return "Pixie";
    }

    @Override
    public String name() {
        return localizedStaticName;
    }

    @Override
    public int shortestMale() {
        return 24;
    }

    @Override
    public int shortestFemale() {
        return 22;
    }

    @Override
    public int heightVariance() {
        return 4;
    }

    @Override
    public int lightestWeight() {
        return 30;
    }

    @Override
    public int weightVariance() {
        return 5;
    }

    @Override
    public long forbiddenWornBits() {
        return 0;
    }

    @Override
    public int availabilityCode() {
        return Area.THEME_FANTASY | Area.THEME_SKILLONLYMASK;
    }

    @Override
    public String racialCategory() {
        return localizedStaticRacialCat;
    }

    @Override
    public int[] bodyMask() {
        return parts;
    }

    @Override
    public String[] racialAbilityNames() {
        return racialAbilityNames;
    }

    @Override
    public int[] racialAbilityLevels() {
        return racialAbilityLevels;
    }

    @Override
    public int[] racialAbilityProficiencies() {
        return racialAbilityProficiencies;
    }

    @Override
    public boolean[] racialAbilityQuals() {
        return racialAbilityQuals;
    }

    @Override
    public String[] racialAbilityParms() {
        return racialAbilityParms;
    }

    @Override
    public String[] culturalAbilityNames() {
        return culturalAbilityNames;
    }

    @Override
    public int[] culturalAbilityProficiencies() {
        return culturalAbilityProficiencies;
    }

    @Override
    public int getXPAdjustment() {
        return -10;
    }

    @Override
    public void affectCharStats(MOB affectedMOB, CharStats affectableStats) {
        super.affectCharStats(affectedMOB, affectableStats);
        affectableStats.setStat(CharStats.STAT_DEXTERITY, affectableStats.getStat(CharStats.STAT_DEXTERITY) + 4);
        affectableStats.setStat(CharStats.STAT_MAX_DEXTERITY_ADJ, affectableStats.getStat(CharStats.STAT_MAX_DEXTERITY_ADJ) + 4);
        affectableStats.setStat(CharStats.STAT_STRENGTH, affectableStats.getStat(CharStats.STAT_STRENGTH) - 6);
        affectableStats.setStat(CharStats.STAT_MAX_STRENGTH_ADJ, affectableStats.getStat(CharStats.STAT_MAX_STRENGTH_ADJ) - 6);
        affectableStats.setStat(CharStats.STAT_CONSTITUTION, affectableStats.getStat(CharStats.STAT_CONSTITUTION) - 4);
        affectableStats.setStat(CharStats.STAT_MAX_CONSTITUTION_ADJ, affectableStats.getStat(CharStats.STAT_MAX_CONSTITUTION_ADJ) - 4);
        affectableStats.setStat(CharStats.STAT_CHARISMA, affectableStats.getStat(CharStats.STAT_CHARISMA) + 2);
        affectableStats.setStat(CharStats.STAT_MAX_CHARISMA_ADJ, affectableStats.getStat(CharStats.STAT_MAX_CHARISMA_ADJ) + 2);
        affectableStats.setStat(CharStats.STAT_SAVE_POISON, affectableStats.getStat(CharStats.STAT_SAVE_POISON) + 10);
        affectableStats.setStat(CharStats.STAT_SAVE_MAGIC, affectableStats.getStat(CharStats.STAT_SAVE_MAGIC) + 25);
    }

    @Override
    public void affectPhyStats(Physical affected, PhyStats affectableStats) {
        super.affectPhyStats(affected, affectableStats);
    }

    @Override
    public void executeMsg(final Environmental myHost, final CMMsg msg) {
        if ((msg.source() == myHost)
            && (msg.target() instanceof Food)
            && ((((Food) msg.target()).material() & RawMaterial.MATERIAL_MASK) == RawMaterial.MATERIAL_FLESH)
            && (((Food) msg.target()).material() != RawMaterial.RESOURCE_WAX)) {
            if (msg.targetMinor() == CMMsg.TYP_EAT) {
                final Ability A = CMClass.getAbility("Poison_Heartstopper");
                if (A != null)
                    A.invoke(msg.source(), msg.source(), true, 0);
            } else if ((msg.targetMinor() == CMMsg.TYP_GET) || (msg.targetMinor() == CMMsg.TYP_PUSH) || (msg.targetMinor() == CMMsg.TYP_PULL)) {
                final Ability A = CMClass.getAbility("Poison_Hives");
                if (A != null)
                    A.invoke(msg.source(), msg.source(), true, 0);
            }
        }
        super.executeMsg(myHost, msg);
    }

    @Override
    public List<Item> outfit(MOB myChar) {
        if (outfitChoices == null) {
            // Have to, since it requires use of special constructor
            final Armor s1 = CMClass.getArmor("GenShirt");
            if (s1 == null)
                return new Vector<Item>();
            outfitChoices = new Vector<Item>();
            s1.setName(L("a delicate green shirt"));
            s1.setDisplayText(L("a delicate green shirt sits gracefully here."));
            s1.setDescription(L("Obviously fine craftmenship, with sharp folds and intricate designs."));
            s1.text();
            outfitChoices.add(s1);

            final Armor s2 = CMClass.getArmor("GenShoes");
            s2.setName(L("a pair of sandals"));
            s2.setDisplayText(L("a pair of sandals lie here."));
            s2.setDescription(L("Obviously fine craftmenship, these light leather sandals have tiny woodland drawings in them."));
            s2.text();
            outfitChoices.add(s2);

            final Armor p1 = CMClass.getArmor("GenPants");
            p1.setName(L("some delicate leggings"));
            p1.setDisplayText(L("a pair delicate brown leggings sit here."));
            p1.setDescription(L("Obviously fine craftmenship, with sharp folds and intricate designs.  They look perfect for dancing in!"));
            p1.text();
            outfitChoices.add(p1);

            final Armor s3 = CMClass.getArmor("GenBelt");
            outfitChoices.add(s3);
        }
        return outfitChoices;
    }

    @Override
    public Weapon myNaturalWeapon() {
        return funHumanoidWeapon();
    }

    @Override
    public String healthText(MOB viewer, MOB mob) {
        final double pct = (CMath.div(mob.curState().getHitPoints(), mob.maxState().getHitPoints()));

        if (pct < .10)
            return L("^r@x1^r is facing mortality!^N", mob.name(viewer));
        else if (pct < .20)
            return L("^r@x1^r is covered in blood.^N", mob.name(viewer));
        else if (pct < .30)
            return L("^r@x1^r is bleeding badly from lots of wounds.^N", mob.name(viewer));
        else if (pct < .40)
            return L("^y@x1^y has numerous bloody wounds and gashes.^N", mob.name(viewer));
        else if (pct < .50)
            return L("^y@x1^y has some bloody wounds and gashes.^N", mob.name(viewer));
        else if (pct < .60)
            return L("^p@x1^p has a few bloody wounds.^N", mob.name(viewer));
        else if (pct < .70)
            return L("^p@x1^p is cut and bruised.^N", mob.name(viewer));
        else if (pct < .80)
            return L("^g@x1^g has some minor cuts and bruises.^N", mob.name(viewer));
        else if (pct < .90)
            return L("^g@x1^g has a few bruises and scratches.^N", mob.name(viewer));
        else if (pct < .99)
            return L("^g@x1^g has a few small bruises.^N", mob.name(viewer));
        else
            return L("^c@x1^c is in perfect health.^N", mob.name(viewer));
    }

    @Override
    public List<RawMaterial> myResources() {
        synchronized (resources) {
            if (resources.size() == 0) {
                resources.addElement(makeResource
                    (L("a pair of @x1 ears", name().toLowerCase()), RawMaterial.RESOURCE_MEAT));
                resources.addElement(makeResource
                    (L("some @x1 blood", name().toLowerCase()), RawMaterial.RESOURCE_BLOOD));
                resources.addElement(makeResource
                    (L("a pile of @x1 bones", name().toLowerCase()), RawMaterial.RESOURCE_BONE));
            }
        }
        return resources;
    }

}
