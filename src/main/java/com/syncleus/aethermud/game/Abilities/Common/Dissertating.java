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
package com.syncleus.aethermud.game.Abilities.Common;

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Abilities.interfaces.ItemCraftor.ItemKeyPair;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Items.interfaces.*;
import com.syncleus.aethermud.game.Libraries.interfaces.ExpertiseLibrary;
import com.syncleus.aethermud.game.Locales.interfaces.Room;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMParms;
import com.syncleus.aethermud.game.core.CMProps;
import com.syncleus.aethermud.game.core.interfaces.Physical;
import com.syncleus.aethermud.game.core.interfaces.Tickable;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


public class Dissertating extends CraftingSkill {
    private final static String localizedName = CMLib.lang().L("Dissertating");
    private static final String[] triggerStrings = I(new String[]{"DISSERTATE", "DISSERTATING"});
    protected Ability theSpell = null;
    protected Scroll fromTheScroll = null;

    @Override
    public String ID() {
        return "Dissertating";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    public String[] triggerStrings() {
        return triggerStrings;
    }

    @Override
    protected ExpertiseLibrary.SkillCostDefinition getRawTrainingCost() {
        return CMProps.getNormalSkillGainCost(ID());
    }

    @Override
    public String supportedResourceString() {
        return "MISC";
    }

    @Override
    public boolean tick(Tickable ticking, int tickID) {
        if ((affected != null) && (affected instanceof MOB) && (tickID == Tickable.TICKID_MOB)) {
            if ((buildingI == null)
                || (theSpell == null)) {
                aborted = true;
                unInvoke();
            }
        }
        return super.tick(ticking, tickID);
    }

    @Override
    public String parametersFile() {
        return "";
    }

    @Override
    protected List<List<String>> loadRecipes() {
        return new ArrayList<List<String>>();
    }

    @Override
    public void unInvoke() {
        if (canBeUninvoked()) {
            if (affected instanceof MOB) {
                final MOB mob = (MOB) affected;
                if ((buildingI != null) && (!aborted)) {
                    if (messedUp)
                        commonTell(mob, L("You got writer`s block! Your dissertation on @x1 fails!", buildingI.name()));
                    else {
                        int theSpellLevel = spellLevel(mob, theSpell);
                        if (fromTheScroll != null)
                            eraseFromScrollItem(fromTheScroll, theSpell, theSpellLevel);
                        buildingI = buildScrollItem(buildingI, theSpell, theSpellLevel);
                        if (buildingI.secretIdentity().length() == 0)
                            setBrand(mob, buildingI);
                        final Room R = mob.location();
                        if (R != null)
                            R.send(mob, CMClass.getMsg(mob, buildingI, this, CMMsg.MSG_WROTE, null, CMMsg.MSG_WROTE, theSpell.ID(), -1, null));

                    }
                }
                buildingI = null;
            }
        }
        super.unInvoke();
    }

    protected void eraseFromScrollItem(Scroll buildingI, Ability theSpell, int level) {
        if (buildingI == null)
            return;
        StringBuilder newList = new StringBuilder();
        buildingI.setBaseValue(buildingI.baseGoldValue() - (100 * CMLib.ableMapper().lowestQualifyingLevel(theSpell.ID())));
        if (buildingI.baseGoldValue() <= 0)
            buildingI.setBaseValue(1);
        for (Ability A : buildingI.getSpells()) {
            if (!A.ID().equalsIgnoreCase(theSpell.ID()))
                newList.append(A.ID()).append(";");
        }
        buildingI.setSpellList(newList.toString());
        this.setName(buildingI);
        if (buildingI.usesRemaining() > 1)
            buildingI.setUsesRemaining(buildingI.usesRemaining() - 1);
        buildingI.text();
    }

    protected int spellLevel(MOB mob, Ability A) {
        int lvl = CMLib.ableMapper().qualifyingLevel(mob, A);
        if (lvl < 0)
            lvl = CMLib.ableMapper().lowestQualifyingLevel(A.ID());
        switch (lvl) {
            case 0:
                return lvl;
            case 1:
                return lvl;
            case 2:
                return lvl + 1;
            case 3:
                return lvl + 1;
            case 4:
                return lvl + 2;
            case 5:
                return lvl + 2;
            case 6:
                return lvl + 3;
            case 7:
                return lvl + 3;
            case 8:
                return lvl + 4;
            case 9:
                return lvl + 4;
            default:
                return lvl + 5;
        }
    }

    @Override
    public ItemKeyPair craftItem(String recipe) {
        return craftItem(recipe, 0, false);
    }

    protected void setName(Scroll buildingI) {
        int x = buildingI.Name().indexOf(L("on the study of"));
        if (x > 0) {
            buildingI.setName(buildingI.Name().substring(0, x).trim());
            buildingI.setDisplayText(L("@x1 sits here.", buildingI.Name()));
            buildingI.setDescription("");
        }
        if (buildingI.getSpells().size() > 0) {
            Ability theSpell = buildingI.getSpells().get(0);
            buildingI.setName(L("@x1 on the study of @x2", buildingI.Name(), theSpell.Name()));
            buildingI.setDisplayText(L("@x1 sits here.", buildingI.Name()));
            String x1 = "";
            String x2 = "";
            switch (CMLib.dice().roll(1, 4, 0)) {
                case 1:
                    x1 = L("short");
                    break;
                case 2:
                    x1 = L("lengthy");
                    break;
                case 3:
                    x1 = L("wordy");
                    break;
                case 4:
                    x1 = L("verbose");
                    break;
            }
            switch (CMLib.dice().roll(1, 4, 0)) {
                case 1:
                    x1 = L("short");
                    break;
                case 2:
                    x1 = L("lengthy");
                    break;
                case 3:
                    x1 = L("wordy");
                    break;
                case 4:
                    x1 = L("verbose");
                    break;
            }
            buildingI.setDescription(L("a @x1 thesis on the @x2 application of @x3", x1, x2, theSpell.Name()));
        }
    }

    protected Scroll buildScrollItem(Item oldBuildingI, Ability theSpell, int level) {
        Scroll buildingI = (Scroll) CMClass.getItem("GenDissertation");
        StringBuilder newList = new StringBuilder(theSpell.ID());
        buildingI.setSpellList(newList.toString());
        setName(buildingI);
        if (buildingI.basePhyStats().level() < level) {
            buildingI.basePhyStats().setLevel(level);
            buildingI.phyStats().setLevel(level);
            buildingI.recoverPhyStats();
        }
        buildingI.setBaseValue(buildingI.baseGoldValue() + (100 * CMLib.ableMapper().lowestQualifyingLevel(theSpell.ID())));
        buildingI.setDescription("");
        buildingI.setUsesRemaining(1);
        buildingI.text();
        return buildingI;
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        return autoGenInvoke(mob, commands, givenTarget, auto, asLevel, 0, false, new Vector<Item>(0));
    }

    @Override
    protected boolean autoGenInvoke(final MOB mob, List<String> commands, Physical givenTarget, final boolean auto,
                                    final int asLevel, int autoGenerate, boolean forceLevels, List<Item> crafted) {
        if (super.checkStop(mob, commands))
            return true;

        if (autoGenerate > 0) {
            final Ability theSpell = mob.fetchRandomAbility();
            if (theSpell == null)
                return false;
            final int level = spellLevel(mob, theSpell);
            buildingI = buildScrollItem(null, theSpell, level);
            crafted.add(buildingI);
            return true;
        }
        randomRecipeFix(mob, addRecipes(mob, loadRecipes()), commands, 0);
        if (commands.size() < 1) {
            commonEmote(mob, L("You must specify what skill to write about, and the paper to write the dissertation on."));
            return false;
        }
        final String pos = commands.get(commands.size() - 1);
        if ((!auto) && (commands.size() < 2)) {
            commonEmote(mob, L("You must specify what skill to write about, and the paper to write the dissertation on."));
            return false;
        } else {
            buildingI = getTarget(mob, null, givenTarget, CMParms.parse(pos), Wearable.FILTER_UNWORNONLY);
            commands.remove(pos);
            if (buildingI == null)
                return false;
            if (!mob.isMine(buildingI)) {
                commonTell(mob, L("You'll need to pick that up first."));
                return false;
            }
            if ((((buildingI.material() & RawMaterial.MATERIAL_MASK) != RawMaterial.MATERIAL_PAPER))
                && (buildingI.material() != RawMaterial.RESOURCE_HIDE)
                && (buildingI.material() != RawMaterial.RESOURCE_HEMP)
                && (buildingI.material() != RawMaterial.RESOURCE_SILK)) {
                commonTell(mob, L("@x1 isn't even made of paper or silk!", buildingI.name(mob)));
                return false;
            }
            if (((buildingI instanceof MiscMagic))
                || (!buildingI.isGeneric())) {
                commonTell(mob, L("There's can't write a dissertation on @x1!", buildingI.name(mob)));
                return false;
            }
            if (buildingI instanceof Scroll) {
                if (((Scroll) buildingI).getSpells().size() > 0) {
                    commonTell(mob, L("You can only write on blank scrolls."));
                    return false;
                }
            } else if (buildingI.readableText().length() > 0) {
                commonTell(mob, L("You can only write on blank paper."));
                return false;
            }
            String recipeName = CMParms.combine(commands, 0);
            theSpell = null;
            fromTheScroll = null;
            String ingredient = "";
            {
                Ability A = (Ability) CMLib.english().fetchEnvironmental(mob.abilities(), recipeName, true);
                if (A == null)
                    A = (Ability) CMLib.english().fetchEnvironmental(mob.abilities(), recipeName, false);
                if ((A != null)
                    && (xlevel(mob) >= spellLevel(mob, A))
                    && (A.name().equalsIgnoreCase(recipeName))) {
                    theSpell = A;
                }
            }
            int manaToLose = 10;
            int experienceToLose = 0;
            if (theSpell == null) {
                int x = CMParms.indexOfIgnoreCase(commands, "from");
                if ((x > 0) && (x < commands.size() - 1)) {
                    recipeName = CMParms.combine(commands, 0, x);
                    String otherScrollName = CMParms.combine(commands, x + 1, commands.size());
                    Item scrollFromI = getTarget(mob, null, givenTarget, CMParms.parse(otherScrollName), Wearable.FILTER_UNWORNONLY);
                    if (scrollFromI == null)
                        return false;
                    if (!mob.isMine(scrollFromI)) {
                        commonTell(mob, L("You'll need to pick that up first."));
                        return false;
                    }
                    if ((!(scrollFromI instanceof Scroll))
                        || (scrollFromI instanceof MiscMagic)) {
                        commonTell(mob, L("@x1 is not a scroll!", scrollFromI.name(mob)));
                        return false;
                    }
                    if ((!(scrollFromI instanceof Scroll))
                        || (((Scroll) scrollFromI).getSpells().size() == 0)) {
                        commonTell(mob, L("@x1 has nothing on it!", scrollFromI.name(mob)));
                        return false;
                    }
                    ingredient = "";
                    for (Ability A : ((Scroll) scrollFromI).getSpells()) {
                        if ((A != null)
                            && (xlevel(mob) >= spellLevel(mob, A))
                            && (A.name().equalsIgnoreCase(recipeName)))
                            theSpell = A;
                    }
                    if (theSpell == null) {
                        commonTell(mob, L("You can't copy the dissertation on '@x1' from the scroll @x2!", recipeName, scrollFromI.name(mob)));
                        return false;
                    }
                    fromTheScroll = (Scroll) scrollFromI;
                } else if (theSpell == null) {
                    commonTell(mob, L("You don't know how to write a dissertation on '@x1'.  Try \"SKILLS\" for a list.", recipeName));
                    return false;
                }
                manaToLose += spellLevel(mob, theSpell) * 10;
            } else {
                manaToLose += CMLib.ableMapper().qualifyingLevel(mob, theSpell) * 10;
                manaToLose -= CMLib.ableMapper().qualifyingClassLevel(mob, theSpell) * 5;
                experienceToLose += 10 + CMLib.ableMapper().qualifyingLevel(mob, theSpell);
                experienceToLose -= CMLib.ableMapper().qualifyingClassLevel(mob, theSpell);
                if (experienceToLose < CMLib.ableMapper().qualifyingLevel(mob, theSpell))
                    experienceToLose = CMLib.ableMapper().qualifyingLevel(mob, theSpell);
            }

            final int resourceType = (ingredient.length() == 0) ? -1 : RawMaterial.CODES.FIND_IgnoreCase(ingredient);

            int[][] data = null;
            if (resourceType > 0) {
                final int[] pm = {resourceType};
                data = fetchFoundResourceData(mob,
                    1, ingredient, pm,
                    0, null, null,
                    bundling,
                    -1,
                    null);
                if (data == null)
                    return false;
            }
            if (manaToLose < 10)
                manaToLose = 10;

            if (mob.curState().getMana() < manaToLose) {
                commonTell(mob, L("You need at least @x1 mana to accomplish that.", "" + manaToLose));
            }

            if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
                return false;

            mob.curState().adjMana(-manaToLose, mob.maxState());

            if ((resourceType > 0) && (data != null))
                CMLib.materials().destroyResourcesValue(mob.location(), data[0][FOUND_AMT], data[0][FOUND_CODE], 0, null);

            playSound = null;
            if (experienceToLose > 0) {
                experienceToLose = getXPCOSTAdjustment(mob, experienceToLose);
                CMLib.leveler().postExperience(mob, null, null, -experienceToLose, false);
                commonTell(mob, L("You lose @x1 experience points for the effort.", "" + experienceToLose));
            }

            int duration = getDuration(100 + (CMLib.ableMapper().qualifyingLevel(mob, theSpell) * 10), mob, CMLib.ableMapper().lowestQualifyingLevel(theSpell.ID()), 10);
            if (duration < 10)
                duration = 10;
            messedUp = !proficiencyCheck(mob, 0, auto);

            String msgStr;
            if (fromTheScroll != null) {
                msgStr = L("<S-NAME> start(s) copying a dissertation on @x1 from @x2 to @x3.", theSpell.name(), fromTheScroll.name(), buildingI.name());
                displayText = L("You are copying a dissertation on @x1 from @x2 to @x3", theSpell.name(), fromTheScroll.name(), buildingI.name());
                verb = L("copying a dissertation on @x1 from @x2 to @x3", theSpell.name(), fromTheScroll.name(), buildingI.name());
            } else {
                msgStr = L("<S-NAME> start(s) writing a dissertation on @x1 onto @x2.", theSpell.name(), buildingI.name());
                displayText = L("You are writing a dissertation on @x1 onto @x2", theSpell.name(), buildingI.name());
                verb = L("writing a dissertation on @x1 onto @x2", theSpell.name(), buildingI.name());
            }
            final CMMsg msg = CMClass.getMsg(mob, buildingI, this, getActivityMessageType(), msgStr);
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                buildingI = (Item) msg.target();
                beneficialAffect(mob, mob, asLevel, duration);
            }
        }
        return true;
    }
}
