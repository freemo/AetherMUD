package com.planet_ink.coffee_mud.MOBS;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class StdShopKeeper extends StdMOB implements ShopKeeper
{
	public String ID(){return "StdShopKeeper";}
	protected int whatISell=0;
	protected Vector storeInventory=new Vector();
	protected Vector baseInventory=new Vector();
	protected Hashtable duplicateInventory=new Hashtable();
	protected int maximumDuplicatesBought=5;
	protected Hashtable prices=new Hashtable();

	public StdShopKeeper()
	{
		super();
		Username="a shopkeeper";
		setDescription("He\\`s pleased to be of assistance.");
		setDisplayText("A shopkeeper is waiting to serve you.");
		setAlignment(1000);
		setMoney(0);
		baseEnvStats.setWeight(150);
		setWimpHitPoint(0);

		baseCharStats().setStat(CharStats.INTELLIGENCE,16);
		baseCharStats().setStat(CharStats.CHARISMA,25);

		baseEnvStats().setArmor(0);

		baseState.setHitPoints(1000);

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}

	public Environmental newInstance()
	{
		return new StdShopKeeper();
	}

	public int whatIsSold(){return whatISell;}
	public void setWhatIsSold(int newSellCode){whatISell=newSellCode;}

	protected boolean inBaseInventory(Environmental thisThang)
	{
		for(int x=0;x<baseInventory.size();x++)
		{
			Environmental E=(Environmental)baseInventory.elementAt(x);
			if((thisThang.isGeneric())&&(E.isGeneric()))
			{
				if(thisThang.Name().equals(E.Name()))
					return true;
			}
			else
			if(CMClass.className(thisThang).equals(CMClass.className(E)))
				return true;
		}
		return false;
	}

	public void addStoreInventory(Environmental thisThang)
	{
		addStoreInventory(thisThang,1,-1);
	}

	public int baseStockSize()
	{
		return baseInventory.size();
	}

	public int totalStockSize()
	{
		return storeInventory.size();
	}

	public void clearStoreInventory()
	{
		storeInventory.clear();
		baseInventory.clear();
		duplicateInventory.clear();
	}

	public Vector getUniqueStoreInventory()
	{
		Vector V=new Vector();
		Environmental lastE=null;
		for(int x=0;x<storeInventory.size();x++)
		{
			Environmental E=(Environmental)storeInventory.elementAt(x);
			boolean ok=true;

			if(lastE!=null)
			{
				if((lastE.isGeneric())&&(E.isGeneric()))
				{
					if(E.Name().equals(lastE.Name()))
						ok=false;
				}
				else
				if(lastE.ID().equals(E.ID()))
					ok=false;
			}

			if(ok)
			for(int v=0;v<V.size();v++)
			{
				Environmental EE=(Environmental)V.elementAt(v);
				if((EE.isGeneric())&&(E.isGeneric()))
				{
					if(E.Name().equals(EE.Name()))
					{
						ok=false;
						break;
					}
				}
				else
				if(EE.ID().equals(E.ID()))
					ok=false;
			}

			if(ok)
			{
				V.addElement(E);
				lastE=E;
			}
		}
		return V;
	}
	public Vector getBaseInventory()
	{
		return baseInventory;
	}

	public String storeKeeperString()
	{
		switch(whatISell)
		{
		case DEAL_ANYTHING:
			return "*Anything*";
		case DEAL_GENERAL:
			return "General items";
		case DEAL_ARMOR:
			return "Armor";
		case DEAL_MAGIC:
			return "Miscellaneous Magic Items";
		case DEAL_WEAPONS:
			return "Weapons";
		case DEAL_PETS:
			return "Pets";
		case DEAL_LEATHER:
			return "Leather";
		case DEAL_INVENTORYONLY:
			return "Only my Inventory";
		case DEAL_TRAINER:
			return "Training in skills/spells/prayers/songs";
		case DEAL_CASTER:
			return "Caster of spells/prayers";
		case DEAL_ALCHEMIST:
			return "Potions";
		case DEAL_INNKEEPER:
			return "My services as an Inn Keeper";
		case DEAL_JEWELLER:
			return "Precious stones and jewellery";
		case DEAL_BANKER:
			return "My services as a Banker";
		case DEAL_CLANBANKER:
			return "My services as a Banker to Clans";
		case DEAL_LANDSELLER:
			return "Real estate";
		case DEAL_CLANDSELLER:
			return "Clan estates";
		case DEAL_ANYTECHNOLOGY:
			return "Any technology";
		case DEAL_BUTCHER:
			return "Meats";
		case DEAL_FOODSELLER:
			return "Foodstuff";
		case DEAL_GROWER:
			return "Vegetables";
		case DEAL_HIDESELLER:
			return "Hides and Furs";
		case DEAL_LUMBERER:
			return "Lumber";
		case DEAL_METALSMITH:
			return "Metal ores";
		case DEAL_STONEYARDER:
			return "Stone and rock";
		default:
			return "... I have no idea WHAT I sell";
		}
	}

	public void addStoreInventory(Environmental thisThang, int number, int price)
	{
		if((whatISell==DEAL_INVENTORYONLY)&&(!inBaseInventory(thisThang)))
			baseInventory.addElement(thisThang.copyOf());
		if(prices.containsKey(thisThang.ID()+"/"+thisThang.name()))
			prices.remove(thisThang.ID()+"/"+thisThang.name());
		prices.put(thisThang.ID()+"/"+thisThang.name(),new Integer(price));
		if(thisThang instanceof InnKey)
		{
			for(int v=0;v<number;v++)
			{
				Environmental copy=thisThang.copyOf();
				((InnKey)copy).hangOnRack(this);
				storeInventory.addElement(copy);
			}
		}
		else
		{
			Environmental copy=thisThang.copyOf();
			storeInventory.addElement(copy);
			if(number>maximumDuplicatesBought)
				maximumDuplicatesBought=number;
			if(number>1)
				duplicateInventory.put(copy,new Integer(number));
		}
	}

	public void delStoreInventory(Environmental thisThang)
	{
		if((whatISell==DEAL_INVENTORYONLY)&&(inBaseInventory(thisThang)))
		{
			for(int v=baseInventory.size()-1;v>=0;v--)
			{
				Environmental E=(Environmental)baseInventory.elementAt(v);
				if((thisThang.isGeneric())&&(E.isGeneric()))
				{
					if(thisThang.Name().equals(E.Name()))
						baseInventory.removeElement(E);
				}
				else
				if(thisThang.ID().equals(E.ID()))
					baseInventory.removeElement(E);
			}
		}
		for(int v=storeInventory.size()-1;v>=0;v--)
		{
			Environmental E=(Environmental)storeInventory.elementAt(v);
			if((thisThang.isGeneric())&&(E.isGeneric()))
			{
				if(thisThang.Name().equals(E.Name()))
				{
					storeInventory.removeElement(E);
					duplicateInventory.remove(E);
				}
			}
			else
			if(thisThang.ID().equals(E.ID()))
			{
				storeInventory.removeElement(E);
				duplicateInventory.remove(E);
			}
		}
		prices.remove(thisThang.ID()+"/"+thisThang.name());
	}

	public boolean doISellThis(Environmental thisThang)
	{
		if(thisThang==null)
			return false;
		switch(whatISell)
		{
		case DEAL_ANYTHING:
			return true;
		case DEAL_ARMOR:
			return (thisThang instanceof Armor);
		case DEAL_MAGIC:
			return (thisThang instanceof MiscMagic);
		case DEAL_WEAPONS:
			return (thisThang instanceof Weapon);
		case DEAL_GENERAL:
			return ((thisThang instanceof Item)
					&&(!(thisThang instanceof Armor))
					&&(!(thisThang instanceof MiscMagic))
					&&(!(thisThang instanceof ClanItem))
					&&(!(thisThang instanceof Weapon))
					&&(!(thisThang instanceof MOB))
					&&(!(thisThang instanceof EnvResource))
					&&(!(thisThang instanceof Ability)));
		case DEAL_LEATHER:
			return ((thisThang instanceof Item)
					&&((((Item)thisThang).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_LEATHER)
					&&(!(thisThang instanceof EnvResource)));
		case DEAL_PETS:
			return (thisThang instanceof MOB);
		case DEAL_INVENTORYONLY:
			return (inBaseInventory(thisThang));
		case DEAL_INNKEEPER:
			return thisThang instanceof InnKey;
		case DEAL_JEWELLER:
			return ((thisThang instanceof Item)
					&&(!(thisThang instanceof Weapon))
					&&(!(thisThang instanceof MiscMagic))
					&&(!(thisThang instanceof ClanItem))
					&&(((((Item)thisThang).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_GLASS)
					||((((Item)thisThang).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_PRECIOUS)
					||((Item)thisThang).fitsOn(Item.ON_EARS)
					||((Item)thisThang).fitsOn(Item.ON_NECK)
					||((Item)thisThang).fitsOn(Item.ON_RIGHT_FINGER)
					||((Item)thisThang).fitsOn(Item.ON_LEFT_FINGER)));
		case DEAL_ALCHEMIST:
			return (thisThang instanceof Potion);
		case DEAL_LANDSELLER:
		case DEAL_CLANDSELLER:
			return (thisThang instanceof LandTitle);
		case DEAL_ANYTECHNOLOGY:
			return (thisThang instanceof Electronics);
		case DEAL_BUTCHER:
			return ((thisThang instanceof EnvResource)
				&&(((EnvResource)thisThang).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_FLESH);
		case DEAL_FOODSELLER:
			return (((thisThang instanceof Food)||(thisThang instanceof Drink))
					&&(!(thisThang instanceof EnvResource)));
		case DEAL_GROWER:
			return ((thisThang instanceof EnvResource)
				&&(((EnvResource)thisThang).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_VEGETATION);
		case DEAL_HIDESELLER:
			return ((thisThang instanceof EnvResource)
				&&((((EnvResource)thisThang).material()==EnvResource.RESOURCE_HIDE)
				||(((EnvResource)thisThang).material()==EnvResource.RESOURCE_FEATHERS)
				||(((EnvResource)thisThang).material()==EnvResource.RESOURCE_LEATHER)
				||(((EnvResource)thisThang).material()==EnvResource.RESOURCE_SCALES)
				||(((EnvResource)thisThang).material()==EnvResource.RESOURCE_WOOL)
				||(((EnvResource)thisThang).material()==EnvResource.RESOURCE_FUR)));
		case DEAL_LUMBERER:
			return ((thisThang instanceof EnvResource)
				&&((((EnvResource)thisThang).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_WOODEN));
		case DEAL_METALSMITH:
			return ((thisThang instanceof EnvResource)
				&&(((((EnvResource)thisThang).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_METAL)
				||(((EnvResource)thisThang).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_MITHRIL));
		case DEAL_STONEYARDER:
			return ((thisThang instanceof EnvResource)
				&&((((EnvResource)thisThang).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_ROCK));
		}

		return false;
	}

	public boolean doIHaveThisInStock(String name, MOB mob)
	{
		Environmental item=EnglishParser.fetchEnvironmental(storeInventory,name,true);
		if(item==null)
			item=EnglishParser.fetchEnvironmental(storeInventory,name,false);
		if((item==null)
		   &&(mob!=null)
		   &&((whatISell==DEAL_LANDSELLER)||(whatISell==DEAL_CLANDSELLER)))
		{
			item=EnglishParser.fetchEnvironmental(addRealEstate(new Vector(),mob),name,true);
			if(item==null)
				item=EnglishParser.fetchEnvironmental(addRealEstate(new Vector(),mob),name,false);
		}
		if(item!=null)
		   return true;
		return false;
	}

	public int stockPrice(Environmental likeThis)
	{
		if(prices.containsKey(likeThis.ID()+"/"+likeThis.name()))
		   return ((Integer)prices.get(likeThis.ID()+"/"+likeThis.name())).intValue();
		return -1;
	}
	public int numberInStock(Environmental likeThis)
	{
		int num=0;
		for(int v=0;v<storeInventory.size();v++)
		{
			Environmental E=(Environmental)storeInventory.elementAt(v);
			if((likeThis.isGeneric())&&(E.isGeneric()))
			{
				if(E.Name().equals(likeThis.Name()))
				{
					Integer possNum=(Integer)duplicateInventory.get(E);
					if(possNum!=null)
						num+=possNum.intValue();
					else
						num++;
				}
			}
			else
			if(E.ID().equals(likeThis.ID()))
			{
				Integer possNum=(Integer)duplicateInventory.get(E);
				if(possNum!=null)
					num+=possNum.intValue();
				else
					num++;
			}
		}

		return num;
	}

	public Environmental getStock(String name, MOB mob)
	{
		Environmental item=EnglishParser.fetchEnvironmental(storeInventory,name,true);
		if(item==null)
			item=EnglishParser.fetchEnvironmental(storeInventory,name,false);
		if((item==null)
		   &&((whatISell==DEAL_LANDSELLER)||(whatISell==DEAL_CLANDSELLER))
		   &&(mob!=null))
		{
			item=EnglishParser.fetchEnvironmental(addRealEstate(new Vector(),mob),name,true);
			if(item==null)
				item=EnglishParser.fetchEnvironmental(addRealEstate(new Vector(),mob),name,false);
		}
		return item;
	}

	public Environmental removeStock(String name, MOB mob)
	{
		Environmental item=getStock(name,mob);
		if(item!=null)
		{
			if(item instanceof Ability)
				return item;

			Integer possNum=(Integer)duplicateInventory.get(item);
			if(possNum!=null)
			{
				duplicateInventory.remove(item);
				int possValue=possNum.intValue();
				possValue--;
				if(possValue>1)
				{
					duplicateInventory.put(item,new Integer(possValue));
					item=item.copyOf();
				}
			}
			else
				storeInventory.removeElement(item);
		}
		return item;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		MOB mob=msg.source();
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_VALUE:
			case CMMsg.TYP_SELL:
			{
				if((msg.tool()!=null)&&(doISellThis(msg.tool())))
				{
					if(yourValue(mob,msg.tool(),false)[0]<2)
					{
						CommonMsgs.say(this,mob,"I'm not interested.",true,false);
						return false;
					}
					if(msg.tool() instanceof Ability)
					{
						CommonMsgs.say(this,mob,"I'm not interested.",true,false);
						return false;
					}
					int numInStock=numberInStock(msg.tool());
					if(((!(msg.tool() instanceof EnvResource))&&(numInStock>=maximumDuplicatesBought))
					||(numInStock>=(maximumDuplicatesBought*100)))
					{
						CommonMsgs.say(this,mob,"I'm sorry, I'm not buying any more of those.",true,false);
						return false;
					}
					if((msg.tool() instanceof Container)&&(((Container)msg.tool()).hasALock()))
					{
						for(int i=0;i<mob.inventorySize();i++)
						{
							Item I=mob.fetchInventory(i);
							if((I!=null)
							&&(I instanceof Key)
							&&(((Key)I).getKey().equals(((Container)msg.tool()).keyName()))&&(I.container()==msg.tool()))
								return true;
						}
						CommonMsgs.say(this,mob,"I won't buy that back unless you put the key in it.",true,false);
						return false;
					}
					if((msg.tool() instanceof Item)&&(msg.source().isMine(msg.tool())))
					{
						FullMsg msg2=new FullMsg(msg.source(),msg.tool(),CMMsg.MSG_DROP,null);
						if(!mob.location().okMessage(mob,msg2))
							return false;
					}
					return super.okMessage(myHost,msg);
				}
				CommonMsgs.say(this,mob,"I'm sorry, I'm not buying those.",true,false);
				return false;
			}
			case CMMsg.TYP_BUY:
			case CMMsg.TYP_VIEW:
			{
				if((msg.tool()!=null)
				&&(doIHaveThisInStock(msg.tool().Name()+"$",mob)))
				{
					if(msg.targetMinor()!=CMMsg.TYP_VIEW)
					{
						int[] val=yourValue(mob,msg.tool(),true);
						if((val[2]>0)&&(val[2]>mob.getExperience()))
						{
							CommonMsgs.say(this,mob,"You aren't experienced enough to buy "+msg.tool().name()+".",false,false);
							return false;
						}
						if((val[1]>0)&&(val[1]>mob.getQuestPoint()))
						{
							CommonMsgs.say(this,mob,"You don't have enough quest points to buy "+msg.tool().name()+".",false,false);
							return false;
						}
						if((val[0]>0)&&(val[0]>MoneyUtils.totalMoney(mob)))
						{
							CommonMsgs.say(this,mob,"You can't afford to buy "+msg.tool().name()+".",false,false);
							return false;
						}
					}
					if(msg.tool() instanceof Item)
					{
						if(((Item)msg.tool()).envStats().level()>mob.envStats().level())
						{
							CommonMsgs.say(this,mob,"That's too advanced for you, I'm afraid.",true,false);
							return false;
						}
					}
					if((msg.tool() instanceof LandTitle)
					&&(whatISell==ShopKeeper.DEAL_CLANDSELLER))
					{
						if(mob.getClanID().length()==0)
						{
							CommonMsgs.say(this,mob,"I only sell land to clans.",true,false);
							return false;
						}
					}
					if(msg.tool() instanceof MOB)
					{
						if(msg.source().totalFollowers()>=msg.source().maxFollowers())
						{
							CommonMsgs.say(this,mob,"You can't accept any more followers.",true,false);
							return false;
						}
					}
					if(msg.tool() instanceof Ability)
					{
						if((whatISell==DEAL_TRAINER)&&(!((Ability)msg.tool()).canBeLearnedBy(new Teacher(),mob)))
							return false;

						if(msg.targetMinor()==CMMsg.TYP_BUY)
						{
							Ability A=(Ability)msg.tool();
							if(A.canTarget(mob)){}
							else
							if(A.canTarget(CMClass.sampleItem()))
							{
								Item I=mob.fetchWieldedItem();
								if(I==null) I=mob.fetchFirstWornItem(Item.HELD);
								if(I==null)
								{
									CommonMsgs.say(this,mob,"You need to be wielding or holding the item you want this cast on.",true,false);
									return false;
								}
							}
							else
							{
								CommonMsgs.say(this,mob,"I don't know how to sell that spell.",true,false);
								return false;
							}
						}
					}
					return super.okMessage(myHost,msg);
				}
				CommonMsgs.say(this,mob,"I don't have that in stock.  Ask for my LIST.",true,false);
				return false;
			}
			case CMMsg.TYP_LIST:
				return super.okMessage(myHost,msg);
			default:
				break;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public String findInnRoom(InnKey key, String addThis, Room R)
	{
		if(R==null) return null;
		String keyNum=key.getKey();
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			if((R.getExitInDir(d)!=null)&&(R.getExitInDir(d).keyName().equals(keyNum)))
			{
				if(addThis.length()>0)
					return addThis+" and to the "+(Directions.getDirectionName(d).toLowerCase());
				else
					return "to the "+(Directions.getDirectionName(d).toLowerCase());
			}
		}
		return null;
	}


	public Vector removeSellableProduct(String named, MOB mob)
	{
		Vector V=new Vector();
		Environmental product=removeStock(named,mob);
		if(product==null) return V;
		V.addElement(product);
		if(product instanceof Container)
		{
			int i=0;
			Key foundKey=null;
			Container C=((Container)product);
			while(i<storeInventory.size())
			{
				int a=storeInventory.size();
				Environmental I=(Environmental)storeInventory.elementAt(i);
				if((I instanceof Item)&&(((Item)I).container()==product))
				{
					if((I instanceof Key)&&(((Key)I).getKey().equals(C.keyName())))
						foundKey=(Key)I;
					((Item)I).unWear();
					V.addElement(I);
					storeInventory.removeElement(I);
					((Item)I).setContainer((Item)product);
				}
				if(a==storeInventory.size())
					i++;
			}
			if((C.isLocked())&&(foundKey==null))
			{
				String keyName=Double.toString(Math.random());
				C.setKeyName(keyName);
				C.setLidsNLocks(C.hasALid(),true,C.hasALock(),false);
				Key key=(Key)CMClass.getItem("StdKey");
				key.setKey(keyName);
				key.setContainer(C);
				V.addElement(key);
			}
		}
		return V;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GIVE:
				if((msg.tool()!=null)
				&&(mob.isASysOp(mob.location()))
				&&((doISellThis(msg.tool()))||(whatISell==DEAL_INVENTORYONLY)))
				{
					Item item2=(Item)msg.tool().copyOf();
					storeInventory.addElement(item2);
					if(item2 instanceof InnKey)
						((InnKey)item2).hangOnRack(this);
					return;
				}
				super.executeMsg(myHost,msg);
				break;
			case CMMsg.TYP_VALUE:
				super.executeMsg(myHost,msg);
				CommonMsgs.say(this,mob,"I'll give you "+yourValue(mob,msg.tool(),false)[0]+" for "+msg.tool().name()+".",true,false);
				break;
			case CMMsg.TYP_SELL:
				super.executeMsg(myHost,msg);
				if((msg.tool()!=null)&&(doISellThis(msg.tool())))
				{
					int val=yourValue(mob,msg.tool(),false)[0];
					mob.setMoney(mob.getMoney()+val);
					mob.recoverEnvStats();
					mob.tell(name()+" pays you "+val+" for "+msg.tool().name()+".");
					if(msg.tool() instanceof Item)
					{
						Item item=(Item)msg.tool();
						Vector V=null;
						if(item instanceof Container)
							V=((Container)item).getContents();
						else
							V=new Vector();
						if(!V.contains(item)) V.addElement(item);
						for(int v=0;v<V.size();v++)
						{
							Item item2=(Item)V.elementAt(v);
							item2.unWear();
							mob.delInventory(item2);
							if(item!=item2)
							{
								item2.setContainer(item);
								storeInventory.addElement(item2);
							}
							else
								storeInventory.addElement(item2);
							if(item2 instanceof InnKey)
								((InnKey)item2).hangOnRack(this);
						}
						item.setContainer(null);
					}
					else
					if(msg.tool() instanceof MOB)
					{
						storeInventory.addElement(((MOB)msg.tool()).copyOf());
						((MOB)msg.tool()).setFollowing(null);
						if((((MOB)msg.tool()).baseEnvStats().rejuv()>0)
						&&(((MOB)msg.tool()).baseEnvStats().rejuv()<Integer.MAX_VALUE)
						&&(((MOB)msg.tool()).getStartRoom()!=null))
							((MOB)msg.tool()).killMeDead(false);
						else
							((MOB)msg.tool()).destroy();
					}
					else
					if(msg.tool() instanceof Ability)
					{

					}
					if(mySession!=null)
						mySession.stdPrintln(msg.source(),msg.target(),msg.tool(),msg.targetMessage());
					if(!Util.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
						mob.location().recoverRoomStats();
				}
				break;
			case CMMsg.TYP_VIEW:
				super.executeMsg(myHost,msg);
				if((msg.tool()!=null)&&(doIHaveThisInStock(msg.tool().Name()+"$",mob)))
				{
					StringBuffer str=new StringBuffer("");
					str.append("Interested in "+msg.tool().name()+"?");
					str.append(" Here is some information for you:");
					str.append("\n\rLevel      : "+msg.tool().envStats().level());
					if(msg.tool() instanceof Item)
					{
						Item I=(Item)msg.tool();
						str.append("\n\rMaterial   : "+Util.capitalize(EnvResource.RESOURCE_DESCS[I.material()&EnvResource.RESOURCE_MASK].toLowerCase()));
						str.append("\n\rWeight     : "+I.envStats().weight()+" pounds");
						if(I instanceof Weapon)
						{
							str.append("\n\rWeap. Type : "+Util.capitalize(Weapon.typeDescription[((Weapon)I).weaponType()]));
							str.append("\n\rWeap. Class: "+Util.capitalize(Weapon.classifictionDescription[((Weapon)I).weaponClassification()]));
						}
						else
						if(I instanceof Armor)
						{
							str.append("\n\rWear Info  : Worn on ");
							for(int l=0;l<20;l++)
							{
								int wornCode=1<<l;
								if(Sense.wornLocation(wornCode).length()>0)
								{
									if(((I.rawProperLocationBitmap()&wornCode)==wornCode))
									{
										str.append(Util.capitalize(Sense.wornLocation(wornCode))+" ");
										if(I.rawLogicalAnd())
											str.append("and ");
										else
											str.append("or ");
									}
								}
							}
							if(str.toString().endsWith(" and "))
								str.delete(str.length()-5,str.length());
							else
							if(str.toString().endsWith(" or "))
								str.delete(str.length()-4,str.length());
						}
					}
					str.append("\n\rDescription: "+msg.tool().description());
					CommonMsgs.say(this,msg.source(),str.toString(),true,false);
				}
				break;
			case CMMsg.TYP_BUY:
				super.executeMsg(myHost,msg);
				if((msg.tool()!=null)
				&&(doIHaveThisInStock(msg.tool().Name()+"$",mob)))
				{
					Vector products=removeSellableProduct(msg.tool().Name()+"$",mob);
					if(products.size()==0) break;
					Environmental product=(Environmental)products.firstElement();
					int[] val=yourValue(mob,product,true);
					if(val[0]>0) MoneyUtils.subtractMoney(this,mob,val[0]);
					if(val[1]>0) mob.setQuestPoint(mob.getQuestPoint()-val[1]);
					if(val[2]>0) MUDFight.postExperience(mob,null,null,-val[2],false);
					mob.recoverEnvStats();
					if(product instanceof Item)
					{
						for(int p=0;p<products.size();p++)
						{
							Item I=(Item)products.elementAt(p);
							mob.location().addItemRefuse(I,Item.REFUSE_PLAYER_DROP);
						}
						FullMsg msg2=new FullMsg(mob,product,this,CMMsg.MSG_GET,null);
						if(location().okMessage(mob,msg2))
						{
							tell(msg.source(),msg.target(),msg.tool(),msg.targetMessage());
							location().send(mob,msg2);
							if((msg.tool() instanceof InnKey)&&(location()!=null))
							{
								InnKey item =(InnKey)msg.tool();
								String buf=findInnRoom(item, "", location());
								if(buf==null) buf=findInnRoom(item, "upstairs", location().getRoomInDir(Directions.UP));
								if(buf==null) buf=findInnRoom(item, "downstairs", location().getRoomInDir(Directions.DOWN));
								if(buf!=null) CommonMsgs.say(this,mob,"Your room is "+buf+".",true,false);
							}
						}
						else
							return;
					}
					else
					if(product instanceof MOB)
					{
						((MOB)product).baseEnvStats().setRejuv(Integer.MAX_VALUE);
						product.recoverEnvStats();
						product.setMiscText(product.text());
						((MOB)product).bringToLife(mob.location(),true);
						CommonMsgs.follow((MOB)product,mob,false);
						if(((MOB)product).amFollowing()==null)
							mob.tell("You cannot accept any more followers!");
					}
					else
					if(product instanceof Ability)
					{
						Ability A=(Ability)product;
						if(whatISell==DEAL_TRAINER)
							A.teach(new Teacher(),mob);
						else
						{
							curState().setMana(maxState().getMana());
							curState().setMovement(maxState().getMovement());
							Vector V=new Vector();
							if(A.canTarget(mob))
							{
								V.addElement(mob.name()+"$");
								A.invoke(this,V,mob,true);
							}
							else
							if(A.canTarget(CMClass.sampleItem()))
							{
								Item I=mob.fetchWieldedItem();
								if(I==null) I=mob.fetchFirstWornItem(Item.HELD);
								if(I==null) I=mob.fetchWornItem("all");
								if(I==null) I=mob.fetchCarried(null,"all");
								if(I==null) return;
								V.addElement(I.name()+"$");
								addInventory(I);
								A.invoke(this,V,I,true);
								delInventory(I);
								if(!mob.isMine(I)) mob.addInventory(I);
							}
							curState().setMana(maxState().getMana());
							curState().setMovement(maxState().getMovement());
						}
					}

					if(mySession!=null)
						mySession.stdPrintln(msg.source(),msg.target(),msg.tool(),msg.targetMessage());
					if(!Util.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
						mob.location().recoverRoomStats();
				}
				break;
			case CMMsg.TYP_LIST:
				{
					super.executeMsg(myHost,msg);
					StringBuffer str=listInventory(mob);
					if(str.length()==0)
					{
						if((whatISell!=DEAL_BANKER)
						&&(whatISell!=DEAL_CLANBANKER))
							CommonMsgs.say(this,mob,"I have nothing for sale.",false,false);
					}
					else
						CommonMsgs.say(this,mob,"\n\r"+str+"^T",true,false);
				}
				break;
			default:
				super.executeMsg(myHost,msg);
				break;
			}
		}
		else
			super.executeMsg(myHost,msg);
	}

	protected double prejudiceValueFromPart(MOB mob, boolean sellTo, String part)
	{
		int x=part.indexOf("=");
		if(x<0) return 0.0;
		String sellorby=part.substring(0,x);
		part=part.substring(x+1);
		if(sellTo&&(!sellorby.trim().equalsIgnoreCase("SELL")))
		   return 0.0;
		if((!sellTo)&&(!sellorby.trim().equalsIgnoreCase("BUY")))
		   return 0.0;
		if(part.trim().indexOf(" ")<0)
			return Util.s_double(part.trim());
		Vector V=Util.parse(part.trim());
		double d=0.0;
		boolean yes=false;
		for(int v=0;v<V.size();v++)
		{
			String bit=(String)V.elementAt(v);
			if(Util.s_double(bit)!=0.0)
				d=Util.s_double(bit);
            if(bit.equalsIgnoreCase(mob.charStats().getCurrentClass().name() ))
			{ yes=true; break;}
			if(bit.equalsIgnoreCase(mob.charStats().getMyRace().racialCategory()))
			{	yes=true; break;}
			if(bit.equalsIgnoreCase(CommonStrings.shortAlignmentStr(mob.getAlignment())))
			{ yes=true; break;}
		}
		if(yes) return d;
		return 0.0;

	}
	protected double prejudiceFactor(MOB mob, boolean sellTo)
	{
		if(prejudiceFactors().length()==0) return 1.0;
		if(prejudiceFactors().indexOf("=")<0)
		{
			if(Util.s_double(prejudiceFactors())!=0.0)
				return Util.s_double(prejudiceFactors());
			return 1.0;
		}
		String factors=prejudiceFactors().toUpperCase();
		int x=factors.indexOf(";");
		while(x>=0)
		{
			String part=factors.substring(0,x).trim();
			factors=factors.substring(x+1).trim();
			double d=prejudiceValueFromPart(mob,sellTo,part);
			if(d!=0.0) return d;
			x=factors.indexOf(";");
		}
		double d=prejudiceValueFromPart(mob,sellTo,factors.trim());
		if(d!=0.0) return d;
		return 1.0;
	}

	public int[] yourValue(MOB mob, Environmental product, boolean sellTo)
	{
		int[] val=new int[3];
		if(product==null) return val;
		Integer I=(Integer)prices.get(product.ID()+"/"+product.name());
		if((I!=null)&&(I.intValue()<=-100))
		{
			if(I.intValue()<=-1000)
				val[2]=(I.intValue()*-1)-1000;
			else
				val[1]=(I.intValue()*-1)-100;
			return val;
		}

		if(product instanceof Item)
			val[0]=((Item)product).value();
		else
		if(product instanceof Ability)
		{
			if(whatISell==DEAL_TRAINER)
				val[0]=CMAble.lowestQualifyingLevel(product.ID())*100;
			else
				val[0]=CMAble.lowestQualifyingLevel(product.ID())*75;
		}
		else
		if(product instanceof MOB)
		{
			Ability A=product.fetchEffect("Prop_Retainable");
			if(A!=null)
				val[0]=Util.s_int(A.text());
			if(val[0]==0)
				val[0]=25*product.envStats().level();
		}
		else
			val[0]=CMAble.lowestQualifyingLevel(product.ID())*25;
		if((I!=null)&&(I.intValue()>=0))
			val[0]=I.intValue();

		if(mob==null) return val;

		double d=prejudiceFactor(mob,sellTo);
		val[0]=(int)Math.round(Util.mul(d,val[0]));

		//double halfPrice=Math.round(Util.div(val,2.0));
		double quarterPrice=Math.round(Util.div(val[0],4.0));

		// gets the shopkeeper a deal on junk.  Pays 25% at 0 charisma, and 50% at 30
		int buyPrice=(int)Math.round(quarterPrice+Util.mul(quarterPrice,Util.div(mob.charStats().getStat(CharStats.CHARISMA),30.0)));

		if((product instanceof EnvResource)&&(numberInStock(product)!=0))
			buyPrice=(int)Math.round(Util.mul(buyPrice,Util.div(((maximumDuplicatesBought*100)-numberInStock(product)),maximumDuplicatesBought*100)));
		else
        if((!(product instanceof Ability)&&(numberInStock(product)!=0)))
			buyPrice=(int)Math.round(Util.mul(buyPrice,Util.div((maximumDuplicatesBought-numberInStock(product)),maximumDuplicatesBought)));

		// the price is 200% at 0 charisma, and 100% at 30
		int sellPrice=(int)Math.round(val[0]+val[0]-Util.mul(val[0],Util.div(mob.charStats().getStat(CharStats.CHARISMA),30.0)));

		if(buyPrice>sellPrice)buyPrice=sellPrice;

		if(sellTo)
			val[0]=sellPrice;
		else
			val[0]=buyPrice;

		if(val[0]<=0) val[0]=1;
		return val;
	}

	protected LandTitle getTitle(Room R)
	{
		for(int a=0;a<R.numEffects();a++)
			if(R.fetchEffect(a) instanceof LandTitle)
				return (LandTitle)R.fetchEffect(a);
		return null;
	}

	protected Vector addRealEstate(Vector V,MOB mob)
	{
		if(((whatISell==DEAL_LANDSELLER)
			||((whatISell==DEAL_CLANDSELLER)&&(mob.getClanID().length()>0)))
		&&(getStartRoom()!=null)
		&&(getStartRoom().getArea()!=null))
		{
			String name=mob.Name();
			if(whatISell==DEAL_CLANDSELLER)
				name=mob.getClanID();
			Vector roomsHandling=new Vector();
			for(Enumeration r=getStartRoom().getArea().getMap();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				LandTitle A=getTitle(R);
				if((A!=null)&&(R.roomID().length()>0)&&(!roomsHandling.contains(R)))
				{
					Vector V2=A.getRooms();
					for(int v=0;v<V2.size();v++)
						roomsHandling.addElement(V2.elementAt(v));
					Item I=CMClass.getItem("GenTitle");
					((LandTitle)I).setLandRoomID(CMMap.getExtendedRoomID(R));
					if(((LandTitle)I).landOwner().equals(name))
					{
						if(!I.Name().endsWith(" (Copy)"))
							I.setName(I.Name()+" (Copy)");
					}
					else
					if(((LandTitle)I).landOwner().length()>0)
						continue;
					else
					{
						boolean skipThisOne=false;
						for(int d=0;d<4;d++)
						{
							Room R2=R.getRoomInDir(d);
							LandTitle L2=null;
							if(R2!=null)
							{
								L2=getTitle(R2);
								if(L2==null)
								{ skipThisOne=false; break;}
							}
							else
								continue;
							if(L2.landOwner().equals(name))
							{ skipThisOne=false; break;}
							if(L2.landOwner().length()>0)
								skipThisOne=true;
						}
						if(skipThisOne) continue;
					}

					I.recoverEnvStats();
					V.addElement(I);
				}
			}
		}
		return V;
	}
	public String prejudiceFactors(){return Util.decompressString(miscText);}
	public void setPrejudiceFactors(String factors){miscText=Util.compressString(factors);}

	protected StringBuffer listInventory(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		int csize=0;
		Vector inventory=getUniqueStoreInventory();
		inventory=addRealEstate(inventory,mob);
		if(inventory.size()==0) return msg;

		int totalCols=((whatISell==DEAL_LANDSELLER)||(whatISell==DEAL_CLANDSELLER))?1:2;
		int totalWidth=60/totalCols;
		int limit=Util.getParmInt(prejudiceFactors(),"LIMIT",0);
		for(int i=0;i<inventory.size();i++)
		{
			Environmental E=(Environmental)inventory.elementAt(i);

			if(!((E instanceof Item)&&((((Item)E).container()!=null)||(!Sense.canBeSeenBy(E,mob)))))
			{
				int[] val=yourValue(mob,E,true);
				if((val[2]>0)&&(((""+val[2]).length()+2)>(4+csize)))
					csize=(""+val[2]).length()-2;
				else
				if((val[1]>0)&&(((""+val[1]).length()+2)>(4+csize)))
					csize=(""+val[1]).length()-2;
				else
				if((""+val[0]).length()>(4+csize))
					csize=(""+val[0]).length()-4;
			}
		}

		String c="^x["+Util.padRight("Cost",4+csize)+"] "+Util.padRight("Product",totalWidth-csize);
		msg.append(c+((totalCols>1)?c:"")+"^.^N\n\r");
		int colNum=0;
		int rowNum=0;
		for(int i=0;i<inventory.size();i++)
		{
			Environmental E=(Environmental)inventory.elementAt(i);

			if(!((E instanceof Item)&&((((Item)E).container()!=null)||(!Sense.canBeSeenBy(E,mob)))))
			{
				int val[]=yourValue(mob,E,true);
				String col=null;
				if(val[1]>0)
					col=Util.padRight("["+val[1]+"qp",5+csize)+"] "+Util.padRight(E.name(),totalWidth-csize);
				else
				if(val[2]>0)
					col=Util.padRight("["+val[2]+"xp",5+csize)+"] "+Util.padRight(E.name(),totalWidth-csize);
				else
					col=Util.padRight("["+val[0],5+csize)+"] "+Util.padRight(E.name(),totalWidth-csize);
				if((++colNum)>totalCols)
				{
					msg.append("\n\r");
					rowNum++;
					if((limit>0)&&(rowNum>limit))
						return msg;
					colNum=1;
				}
				msg.append(col);
			}
		}
		return msg;
	}
}
