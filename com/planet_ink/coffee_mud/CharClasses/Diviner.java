package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


/*
   Copyright 2000-2011 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class Diviner extends SpecialistMage
{
    public String ID(){return "Diviner";}
    public String name(){return "Diviner";}
    public int domain(){return Ability.DOMAIN_DIVINATION;}
    public int opposed(){return Ability.DOMAIN_ILLUSION;}
    public int availabilityCode(){return Area.THEME_FANTASY;}
    public void initializeClass()
    {
        super.initializeClass();
        CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Skill_Spellcraft",false);
        
        
        CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Spell_AnalyzeDweomer",25,true);
        /*NEW*/CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Spell_SolveMaze",25,true);
        /*NEW*/CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Spell_DetectWeaknesses",25,true);
        /*NEW*/CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Spell_NaturalCommunion",25,true);
        CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Spell_DetectTraps",25,true);
        CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Spell_KnowOrigin",25,true);
        CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Spell_DetectScrying",25,true);
        /*NEW*/CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Spell_SpottersOrders",25,"",true,false,CMParms.parseSemicolons("Spell_DetectWeaknesses",true),"");
        CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Spell_Breadcrumbs",25,true);
        /*NEW*/CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Spell_FindDirections",25,true);
        CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Spell_KnowPain",25,true);
        CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Spell_KnowBliss",25,true);
        CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Spell_DetectAmbush",25,true);
        CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Spell_TrueSight",25,true);
        CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Spell_FutureDeath",25,true);
    }

// possibilities:
//CMLib.ableMapper().addCharAbilityMapping(ID(),0,"Spell_Telepathy",25,true); // target mobs thoughts -- mostly from behaviors, class, racial, intelligence
//CMLib.ableMapper().addCharAbilityMapping(ID(),0,"Spell_HearThoughts",25,true); // 8 nearest mobs thoughts -- mostly from behaviors, class, racial, intelligence
//CMLib.ableMapper().addCharAbilityMapping(ID(),0,"Spell_PryingEye",25,true); // give a floating eye creature directions, it follows them and comes back, replaying what it saw on command.
//CMLib.ableMapper().addCharAbilityMapping(ID(),0,"Spell_GroupStatus",25,true); // casts on whole group, reports to caster any change in status (on ground, seriously hurt, dead, etc..)
//CMLib.ableMapper().addCharAbilityMapping(ID(),0,"Spell_SpyingStone",25,true); // casts on stone, left at location.  Caster can go back and see everything it saw.
//CMLib.ableMapper().addCharAbilityMapping(ID(),0,"Spell_DiviningEye",25,true); // Allows the Diviner to use the Prying Eye to cast other divination spells on other targets 
//CMLib.ableMapper().addCharAbilityMapping(ID(),0,"Spell_DeathWarning",25,true); // Receive a warning before you die, and specify what you will do -- including flee.
//CMLib.ableMapper().addCharAbilityMapping(ID(),0,"Spell_ArmsLength",25,true); // Keeps you at range from your opponent during combat. (lasts 3 rounds)
//CMLib.ableMapper().addCharAbilityMapping(ID(),0,"Spell_CombatForsight",25,true); // Specify what you will do and it will simulate combat for you (will loop macro)
}
