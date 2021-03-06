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

import com.syncleus.aethermud.game.Items.interfaces.RawMaterial;
import com.syncleus.aethermud.game.core.CMLib;

/*
   Copyright 2002-2017 Bo Zimmerman

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
public class GenResource extends GenItem implements RawMaterial {
    protected String domainSource = null;

    public GenResource() {
        super();
        setName("a pile of resources");
        setDisplayText("a pile of resources sits here.");
        setDescription("");
        setMaterial(RawMaterial.RESOURCE_IRON);
        basePhyStats().setWeight(0);
        recoverPhyStats();
    }

    @Override
    public String ID() {
        return "GenResource";
    }

    @Override
    public String domainSource() {
        return domainSource;
    }

    @Override
    public void setDomainSource(String src) {
        domainSource = src;
    }

    @Override
    public boolean rebundle() {
        return CMLib.materials().rebundle(this);
    }

    @Override
    public void quickDestroy() {
        CMLib.materials().quickDestroy(this);
    }
}
