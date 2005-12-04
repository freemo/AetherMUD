package com.planet_ink.coffee_mud.Abilities.Common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.File;

/* 
   Copyright 2000-2005 Bo Zimmerman

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

public class Distilling extends Cooking
{
	public String ID() { return "Distilling"; }
	public String name(){ return "Distilling";}
	private static final String[] triggerStrings = {"DISTILLING"};
	public String[] triggerStrings(){return triggerStrings;}
	public String cookWordShort(){return "distill";};
	public String cookWord(){return "distilling";};
	public boolean honorHerbs(){return false;}
    public String supportedResourceString(){return "MISC";}
    protected String defaultFoodSound="hotspring.wav";
    protected String defaultDrinkSound="hotspring.wav";

    protected Vector loadRecipes(){return super.loadRecipes("liquors.txt");}
    
    public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
    {
        if((!super.invoke(mob,commands,givenTarget,auto,asLevel))||(finalDish==null))
            return false;
        Ability A2=finalDish.fetchEffect(0);
        if((A2!=null)
        &&(finalDish instanceof Drink))
            ((Drink)finalDish).setLiquidType(EnvResource.RESOURCE_LIQUOR);
        return true;
    }
}
