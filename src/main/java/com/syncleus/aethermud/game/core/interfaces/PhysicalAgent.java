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
package com.syncleus.aethermud.game.core.interfaces;

/**
 *
 * A Physical object capable of initiating an effect on the world.
 * (Even if it is normally passive)
 * @see com.syncleus.aethermud.game.core.interfaces.Physical
 * @see com.syncleus.aethermud.game.core.interfaces.Behavable
 *
 * @author Bo Zimmerman
 *
 */
public interface PhysicalAgent extends Physical, Behavable {
}
