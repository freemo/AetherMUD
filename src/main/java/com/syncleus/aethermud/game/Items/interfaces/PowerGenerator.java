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
package com.syncleus.aethermud.game.Items.interfaces;

/**
 * An interface that combines both a PowerSource and and FuelConsumer
 * to produce a Power Generator, or converter from fuel to electricity.
 * @see FuelConsumer
 * @see PowerSource
 * @author Bo Zimmerman
 *
 */
public interface PowerGenerator extends PowerSource {
    /**
     * Gets the amount of power generated every tick.  This is fed into
     * the Electronics capacitance.
     * @see Electronics#powerCapacity()
     * @see Electronics#powerRemaining()
     * @see PowerGenerator#setGeneratedAmountPerTick(int)
     * @return the amount of power generated every tick
     */
    public int getGeneratedAmountPerTick();

    /**
     * Sets the amount of power generated every tick.  This is fed into
     * the Electronics capacitance.
     * @see Electronics#powerCapacity()
     * @see Electronics#powerRemaining()
     * @see PowerGenerator#getGeneratedAmountPerTick()
     * @param amt the amount of power generated every tick
     */
    public void setGeneratedAmountPerTick(int amt);
}
