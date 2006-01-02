package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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



import java.util.*;

/* 
   Copyright 2000-2006 Bo Zimmerman

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

public class Chant_SummonHail extends Chant
{
    public String ID() { return "Chant_SummonHail"; }
    public String name(){ return renderedMundane?"hail":"Summon Hail";}
    public int abstractQuality(){return Ability.MALICIOUS;}
    public int maxRange(){return 10;}
    protected int canAffectCode(){return 0;}
    protected int canTargetCode(){return CAN_MOBS;}
    public long flags(){return Ability.FLAG_WEATHERAFFECTING;}

    public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
    {
        if(((mob.location().domainType()&Room.INDOORS)>0)&&(!auto))
        {
            mob.tell("You must be outdoors for this chant to work.");
            return false;
        }
        if((!auto)
        &&((mob.location().getArea().getClimateObj().weatherType(mob.location())!=Climate.WEATHER_WINTER_COLD)
            &&(mob.location().getArea().getClimateObj().weatherType(mob.location())!=Climate.WEATHER_HAIL)))
        {
            mob.tell("This chant requires a cold snap or a hail storm!");
            return false;
        }
        MOB target=this.getTarget(mob,commands,givenTarget);
        if(target==null) return false;

        // the invoke method for spells receives as
        // parameters the invoker, and the REMAINING
        // command line parameters, divided into words,
        // and added as String objects to a vector.
        if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
            return false;


        boolean success=profficiencyCheck(mob,0,auto);

        if(success)
        {
            CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),(auto?"^JHailstones falling from the sky whack <T-NAME>.^?":"^S<S-NAME> chant(s) to <T-NAMESELF>.  Suddenly a volley of hailstones assaults <T-HIM-HER>!^?")+CMProps.msp("hail.wav",40));
            CMMsg msg2=CMClass.getMsg(mob,target,this,verbalCastMask(mob,target,auto)|CMMsg.TYP_WATER,null);
            if((mob.location().okMessage(mob,msg))&&((mob.location().okMessage(mob,msg2))))
            {
                mob.location().send(mob,msg);
                mob.location().send(mob,msg2);
                int maxDie =  (int)Math.round(new Integer(adjustedLevel(mob,asLevel)).doubleValue());
                int damage = CMLib.dice().roll(maxDie,4,0);
                if((msg.value()>0)||(msg2.value()>0))
                    damage = (int)Math.round(CMath.div(damage,2.0));
                if(target.location()==mob.location())
                {
                    Item I=null;
                    for(int i=0;i<target.inventorySize();i++)
                    {
                        I=target.fetchInventory(i);
                        if((I.container()==null)
                        &&(I.amWearingAt(Item.WORN_HEAD))
                        &&(((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL)
                            ||((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL)
                            ||((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL)))
                            break;
                        I=null;
                    }
                    if((I!=null)&&(I.amWearingAt(Item.WORN_HEAD)))
                        target.location().show(target,I,null,CMMsg.MSG_OK_ACTION,"Hailstones bounce harmlessly off <O-NAME> being worn by <S-NAME>.");
                    else
                        CMLib.combat().postDamage(mob,target,this,damage,CMMsg.MASK_GENERAL|CMMsg.TYP_WATER,Weapon.TYPE_BASHING,"The hailstones <DAMAGE> <T-NAME>!");
                }
                if(mob.location().getArea().getClimateObj().weatherType(mob.location())!=Climate.WEATHER_HAIL)
                {
                    mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_HAIL);
                    mob.location().getArea().getClimateObj().forceWeatherTick(mob.location().getArea());
                }
            }
        }
        else
            return maliciousFizzle(mob,target,"<S-NAME> chant(s) at <T-NAMESELF>, but the magic fades.");


        // return whether it worked
        return success;
    }
}
