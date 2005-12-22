package com.planet_ink.coffee_mud.Abilities.Common;
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

public class GatheringSkill extends CommonSkill
{
	public String ID() { return "GatheringSkill"; }
	public String name(){ return "GatheringSkill";}
	private static final String[] triggerStrings = {"FLETCH","FLETCHING"};
	public String[] triggerStrings(){return triggerStrings;}
	public String supportedResourceString(){return "";}
	protected static final Hashtable supportedResources=new Hashtable();
	
	public Vector myresources()
	{
	    if(supportedResources.containsKey(ID()))
	        return (Vector)supportedResources.get(ID());
	    String mask=supportedResourceString();
	    Vector maskV=new Vector();
	    String str=mask;
	    while(mask.length()>0)
	    {
	        str=mask;
	        int x=mask.indexOf("|");
	        if(x>=0)
	        {
	            str=mask.substring(0,x);
	            mask=mask.substring(x+1);
	        }
	        else
	            mask="";
	        if(str.length()>0)
	        {
	            boolean found=false;
        		for(int i=0;i<EnvResource.MATERIAL_DESCS.length;i++)
        			if(EnvResource.MATERIAL_DESCS[i].equalsIgnoreCase(str))
        			{
        			    for(int ii=0;ii<EnvResource.RESOURCE_DATA.length;ii++)
        			        if((EnvResource.RESOURCE_DATA[ii][0]&EnvResource.MATERIAL_MASK)==(i<<8))
			                { 
        			            found=true; 
        			            maskV.addElement(new Integer(EnvResource.RESOURCE_DATA[ii][0]));
        			        }
        			    break;
        			}
	            if(!found)
	            for(int i=0;i<EnvResource.RESOURCE_DESCS.length;i++)
	                if(EnvResource.RESOURCE_DESCS[i].equalsIgnoreCase(str))
	                { 
		                maskV.addElement(new Integer(EnvResource.RESOURCE_DATA[i][0]));
		                break;
		            }
	        }
	    }
	    supportedResources.put(ID(),maskV);
	    return maskV;
	}
	
	public boolean bundle(MOB mob, Vector what)
	{
	    if((what.size()<3)
	    ||(!CMath.isNumber((String)what.elementAt(1))))
	    {
	        commonTell(mob,"You must specify an amount to bundle, followed by what resource to bundle.");
	        return false;
	    }
	    int amount=CMath.s_int((String)what.elementAt(1));
	    if(amount<=0)
	    {
	        commonTell(mob,amount+" is not an appropriate amount.");
	        return false;
	    }
	    int numHere=0;
	    Room R=mob.location();
	    if(R==null) return false;
	    String name=CMParms.combine(what,2);
	    int foundResource=-1;
	    Item foundAnyway=null;
	    Vector maskV=myresources();
	    Hashtable foundAblesH=new Hashtable();
	    Ability A=null;
	    long lowestNonZeroFoodNumber=Long.MAX_VALUE;
	    for(int i=0;i<R.numItems();i++)
	    {
	        Item I=R.fetchItem(i);
			if(CMLib.english().containsString(I.Name(),name))
			{
			    foundAnyway=I;
				if((I instanceof EnvResource)
				&&(!CMLib.flags().isOnFire(I))
				&&(!CMLib.flags().enchanted(I))
				&&(I.container()==null)
				&&((I.material()==foundResource)||(maskV.contains(new Integer(I.material())))))
				{
				    if((I instanceof Food)
				    &&(((Food)I).decayTime()>0)
				    &&(((Food)I).decayTime()<lowestNonZeroFoodNumber))
				        lowestNonZeroFoodNumber=((Food)I).decayTime();
				    for(int a=0;a<I.numEffects();a++)
				    {
				        A=I.fetchEffect(a);
				        if((A!=null)
				        &&(!A.canBeUninvoked())
				        &&(!foundAblesH.containsKey(A.ID())))
				            foundAblesH.put(A.ID(),A);
				    }
				    foundResource=I.material();
				    numHere+=I.envStats().weight();
				}
			}
	    }
	    if((numHere==0)||(foundResource<0))
	    {
	        if(foundAnyway!=null)
		        commonTell(mob,"You can't bundle "+foundAnyway.name()+" with this skill.");
	        else
		        commonTell(mob,"You don't see any "+name+" on the ground here.");
	        return false;
	    }
	    if(numHere<amount)
	    {
	        commonTell(mob,"You only see "+numHere+" pounds of "+name+" on the ground here.");
	        return false;
	    }
	    if(lowestNonZeroFoodNumber==Long.MAX_VALUE)
	        lowestNonZeroFoodNumber=0;
		Item I=(Item)CMLib.utensils().makeResource(foundResource,mob.location().domainType(),true);
        if(I==null)
        {
            commonTell(mob,"You could not bundle "+name+" due to "+foundResource+" being an invalid resource code.  Bug it!");
            return false;
        }
		I.setName("a "+amount+"# "+EnvResource.RESOURCE_DESCS[foundResource&EnvResource.RESOURCE_MASK].toLowerCase()+" bundle");
		I.setDisplayText(I.name()+" is here.");
		I.baseEnvStats().setWeight(amount);
		if(R.show(mob,null,I,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> create(s) <O-NAME>."))
		{
		    int lostValue=destroyResources(R,amount,foundResource,-1,null,0);
			I.setBaseValue(lostValue);
			if(I instanceof Food)
			    ((Food)I).setNourishment(((Food)I).nourishment()*amount);
			if(I instanceof Drink)
			    ((Drink)I).setLiquidHeld(((Drink)I).liquidHeld()*amount);
			R.addItemRefuse(I,Item.REFUSE_PLAYER_DROP);
		}
		if(I instanceof Food)
		    ((Food)I).setDecayTime(lowestNonZeroFoodNumber);
		for(Enumeration e=foundAblesH.keys();e.hasMoreElements();)
		    I.addNonUninvokableEffect((Ability)((Environmental)foundAblesH.get(e.nextElement())).copyOf());
		R.recoverRoomStats();
	    return true;
	}
	
	
}
