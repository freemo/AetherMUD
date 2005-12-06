package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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

public class Baking extends Cooking
{
	public String ID() { return "Baking"; }
	public String name(){ return "Baking";}
	private static final String[] triggerStrings = {"BAKING","BAKE"};
    public String supportedResourceString(){return "MISC";}
	public String[] triggerStrings(){return triggerStrings;}
	public String cookWordShort(){return "bake";};
	public String cookWord(){return "baking";};
	public boolean honorHerbs(){return false;}
	public boolean requireLid(){return true;}

    protected Vector loadRecipes(){return super.loadRecipes("bake.txt");}
}
