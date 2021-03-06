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
package com.syncleus.aethermud.game.Items.Basic;

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Items.interfaces.Food;
import com.syncleus.aethermud.game.Items.interfaces.RawMaterial;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMSecurity;
import com.syncleus.aethermud.game.core.interfaces.Environmental;


public class GenFoodResource extends GenFood implements RawMaterial, Food {
    protected static Ability rot = null;
    protected String domainSource = null;

    public GenFoodResource() {
        super();
        setName("an edible resource");
        setDisplayText("a pile of edible resource sits here.");
        setDescription("");
        material = RawMaterial.RESOURCE_BERRIES;
        setNourishment(200);
        basePhyStats().setWeight(0);
        recoverPhyStats();
        decayTime = 0;
    }

    @Override
    public String ID() {
        return "GenFoodResource";
    }

    @Override
    public void setMaterial(int newValue) {
        super.setMaterial(newValue);
        decayTime = 0;
    }

    @Override
    public void executeMsg(Environmental host, CMMsg msg) {
        super.executeMsg(host, msg);
        if (rot == null) {
            rot = CMClass.getAbility("Prayer_Rot");
            if (rot == null)
                return;
            rot.setAffectedOne(null);
        }
        if (!CMSecurity.isDisabled(CMSecurity.DisFlag.FOODROT))
            rot.executeMsg(this, msg);
    }

    @Override
    public boolean rebundle() {
        return false;
        //CMLib.materials().rebundle(this);
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public void quickDestroy() {
        CMLib.materials().quickDestroy(this);
    }

    @Override
    public boolean okMessage(Environmental host, CMMsg msg) {
        if (rot == null) {
            rot = CMClass.getAbility("Prayer_Rot");
            if (rot == null)
                return true;
            rot.setAffectedOne(null);
        }
        if (!CMSecurity.isDisabled(CMSecurity.DisFlag.FOODROT)) {
            if (!rot.okMessage(this, msg))
                return false;
        }
        return super.okMessage(host, msg);
    }

    @Override
    public String domainSource() {
        return domainSource;
    }

    @Override
    public void setDomainSource(String src) {
        domainSource = src;
    }
}
