package com.planet_ink.coffee_mud.Libraries.interfaces;

import java.util.Vector;
import com.planet_ink.coffee_mud.Common.interfaces.Session;
import com.planet_ink.coffee_mud.core.interfaces.CMObject;
/* 
Copyright 2000-2007 Bo Zimmerman

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

public interface SessionsList extends CMLibrary
{
    public Vector all=new Vector();
    public Session elementAt(int x);
    public int size();
    public void addElement(Session S);
    public void removeElementAt(int x);
    public void removeElement(Session S);
    public void stopSessionAtAllCosts(Session S);
}
