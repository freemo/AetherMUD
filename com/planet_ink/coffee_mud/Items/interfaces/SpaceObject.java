package com.planet_ink.coffee_mud.core.interfaces;

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
public interface SpaceObject extends Environmental
{
	public long[] coordinates();
	public void setCoords(long[] coords);
	public double[] direction();
	public void setDirection(double[] dir);
	public long velocity();
	public void setVelocity(long v);
	public long accelleration();
	public void setAccelleration(long v);
	
	public SpaceObject knownTarget();
	public void setKnownTarget(SpaceObject O);
	public SpaceObject knownSource();
	public void setKnownSource(SpaceObject O);
	public SpaceObject orbiting();
	public void setOrbiting(SpaceObject O);
	
	public static final long DISTANCE_MILE=1;
	public static final long DISTANCE_LIGHTYEAR=2000000000;
	public static final long DISTANCE_AROUNDGALAXY=DISTANCE_LIGHTYEAR*100;
	public static final long DISTANCE_PLANETRADIUS=3976;
	public static final long DISTANCE_ORBITING=3252;

	public static final long VELOCITY_LIGHT=833333;
	public static final long VELOCITY_SUBLIGHT=750000;
	public static final long VELOCITY_SOUND=1;
	public static final long VELOCITY_ORBITING=311;
	public static final long VELOCITY_ESCAPE=442;
	public static final long VELOCITY_WARP1=VELOCITY_LIGHT;
	public static final long VELOCITY_WARP2=VELOCITY_LIGHT*4;
	public static final long VELOCITY_WARP3=VELOCITY_LIGHT*9;
	public static final long VELOCITY_WARP4=VELOCITY_LIGHT*16;
	public static final long VELOCITY_WARP5=VELOCITY_LIGHT*25;
	public static final long VELOCITY_WARP6=VELOCITY_LIGHT*36;
	public static final long VELOCITY_WARP7=VELOCITY_LIGHT*49;
	public static final long VELOCITY_WARP8=VELOCITY_LIGHT*64;
	public static final long VELOCITY_WARP9=VELOCITY_LIGHT*81;
	public static final long VELOCITY_WARP10=VELOCITY_LIGHT*100;
	public static final long VELOCITY_TRANSWARP1=VELOCITY_LIGHT;
	public static final long VELOCITY_TRANSWARP2=VELOCITY_LIGHT*8;
	public static final long VELOCITY_TRANSWARP3=VELOCITY_LIGHT*27;
	public static final long VELOCITY_TRANSWARP4=VELOCITY_LIGHT*64;
	public static final long VELOCITY_TRANSWARP5=VELOCITY_LIGHT*125;
	public static final long VELOCITY_TRANSWARP6=VELOCITY_LIGHT*216;
	public static final long VELOCITY_TRANSWARP7=VELOCITY_LIGHT*343;
	public static final long VELOCITY_TRANSWARP8=VELOCITY_LIGHT*512;
	public static final long VELOCITY_TRANSWARP9=VELOCITY_LIGHT*729;
	public static final long VELOCITY_TRANSWARP10=VELOCITY_LIGHT*1000;
}
