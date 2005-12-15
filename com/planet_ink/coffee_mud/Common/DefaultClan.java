package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.threads.ServiceEngine;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;

/**
 * <p>Portions Copyright (c) 2003 Jeremy Vyska</p>
 * <p>Portions Copyright (c) 2004 Bo Zimmerman</p>
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * <p>you may not use this file except in compliance with the License.
 * <p>You may obtain a copy of the License at
 *
 * <p>       http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * <p>distributed under the License is distributed on an "AS IS" BASIS,
 * <p>WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * <p>See the License for the specific language governing permissions and
 * <p>limitations under the License.
 */
public class DefaultClan implements Clan
{
    public String ID(){return "DefaultClan";}
    private long tickStatus=Tickable.STATUS_NOT;
    public long getTickStatus(){return tickStatus;}
    protected String clanName="";
    protected String clanPremise="";
    protected String clanRecall="";
    protected String clanMorgue="";
    protected String clanDonationRoom="";
    protected int clanTrophies=0;
    protected String AcceptanceSettings="";
    protected int clanType=TYPE_CLAN;
    protected int ClanStatus=0;
    protected Vector voteList=null;
    protected long exp=0;
    protected Vector clanKills=new Vector();
    protected String lastClanKillRecord=null;
    protected double taxRate=0.0;
    
    //*****************
    public Hashtable relations=new Hashtable();
    public int government=GVT_DICTATORSHIP;
    //*****************

    /** return a new instance of the object*/
    public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new DefaultClan();}}
    public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
    public CMObject copyOf()
    {
        try
        {
            return (Clan)this.clone();
        }
        catch(CloneNotSupportedException e)
        {
            return new DefaultClan();
        }
    }

    private synchronized void clanKills()
    {
        if(lastClanKillRecord==null)
        {
            Vector V=CMLib.database().DBReadData(ID(),"CLANKILLS",ID()+"/CLANKILLS");
            clanKills.clear();
            if(V.size()==0)
                lastClanKillRecord="";
            else
            {
                lastClanKillRecord=(String)((Vector)V.firstElement()).elementAt(3);
                V=Util.parseSemicolons(lastClanKillRecord,true);
                for(int v=0;v<V.size();v++)
                    clanKills.addElement(new Long(Util.s_long((String)V.elementAt(v))));
            }
        }
    }
    
    private void updateClanKills()
    {
        Long date=null;
        StringBuffer str=new StringBuffer("");
        for(int i=clanKills.size()-1;i>=0;i--)
        {
            date=(Long)clanKills.elementAt(i);
            if(date.longValue()<(System.currentTimeMillis()))
                clanKills.removeElementAt(i);
            else
                str.append(date.longValue()+";");
        }
        if((lastClanKillRecord==null)||(!lastClanKillRecord.equals(str.toString())))
        {
            lastClanKillRecord=str.toString();
            CMLib.database().DBDeleteData(ID(),"CLANKILLS",ID()+"/CLANKILLS");
            try{Thread.sleep(200);}catch(Exception e){}
            CMLib.database().DBCreateData(ID(),"CLANKILLS",ID()+"/CLANKILLS",str.toString());
        }
    }
    
    public void updateVotes()
    {
        CMLib.database().DBDeleteData(ID(),"CLANVOTES",ID()+"/CLANVOTES");
        StringBuffer str=new StringBuffer("");
        for(Enumeration e=votes();e.hasMoreElements();)
        {
            ClanVote CV=(ClanVote)e.nextElement();
            str.append(CMLib.xml().convertXMLtoTag("BY",CV.voteStarter));
            str.append(CMLib.xml().convertXMLtoTag("FUNC",CV.function));
            str.append(CMLib.xml().convertXMLtoTag("ON",""+CV.voteStarted));
            str.append(CMLib.xml().convertXMLtoTag("STATUS",""+CV.voteStatus));
            str.append(CMLib.xml().convertXMLtoTag("CMD",CV.matter));
            if((CV.votes!=null)&&(CV.votes.size()>0))
            {
                str.append("<VOTES>");
                for(int v=0;v<CV.votes.size();v++)
                {
                    str.append(CMLib.xml().convertXMLtoTag("BY",(String)CV.votes.elementAt(v,1)));
                    str.append(CMLib.xml().convertXMLtoTag("YN",""+((Boolean)CV.votes.elementAt(v,2)).booleanValue()));
                }
                str.append("</VOTES>");
            }
        }
        if(str.length()>0)
            CMLib.database().DBCreateData(ID(),"CLANVOTES",ID()+"/CLANVOTES","<BALLOTS>"+str.toString()+"</BALLOTS>");
    }
    public void addVote(Object CV)
    {
        if(!(CV instanceof ClanVote))
            return;
        votes();
        voteList.addElement(CV);
    }
    public void delVote(Object CV)
    {
        votes();
        voteList.removeElement(CV);
    }

    public void recordClanKill()
    {
        clanKills();
        clanKills.addElement(new Long(System.currentTimeMillis()));
        updateClanKills();
    }
    public int getCurrentClanKills()
    {
        clanKills();
        return clanKills.size();
    }
    
    public long calculateMapPoints()
    {
        return calculateMapPoints(getControlledAreas());
    }
    public long calculateMapPoints(Vector controlledAreas)
    {
        long points=0;
        for(Enumeration e=controlledAreas.elements();e.hasMoreElements();)
        {
            Area A=(Area)e.nextElement();
            LegalBehavior B=CMLib.utensils().getLegalBehavior(A);
            if(B!=null)
                points+=B.controlPoints();
        }
        return points;
    }

    public Vector getControlledAreas()
    {
        Vector done=new Vector();
        for(Enumeration e=CMLib.map().areas();e.hasMoreElements();)
        {
            Area A=(Area)e.nextElement();
            LegalBehavior B=CMLib.utensils().getLegalBehavior(A);
            if(B!=null)
            {
                String controller=B.rulingClan();
                Area A2=CMLib.utensils().getLegalObject(A);
                if(controller.equals(ID())&&(!done.contains(A2)))
                    done.addElement(A2);
            }
        }
        return done;
    }

    public Enumeration votes()
    {
        if(voteList==null)
        {
            Vector V=CMLib.database().DBReadData(ID(),"CLANVOTES",ID()+"/CLANVOTES");
            voteList=new Vector();
            for(int v=0;v<V.size();v++)
            {
                ClanVote CV=new ClanVote();
                String rawxml=(String)((Vector)V.elementAt(v)).elementAt(3);
                if(rawxml.trim().length()==0) return voteList.elements();
                Vector xml=CMLib.xml().parseAllXML(rawxml);
                if(xml==null)
                {
                    Log.errOut("Clans","Unable to parse: "+rawxml);
                    return voteList.elements();
                }
                Vector voteData=CMLib.xml().getRealContentsFromPieces(xml,"BALLOTS");
                if(voteData==null){ Log.errOut("Clans","Unable to get BALLOTS data."); return voteList.elements();}
                CV.voteStarter=CMLib.xml().getValFromPieces(voteData,"BY");
                CV.voteStarted=CMLib.xml().getLongFromPieces(voteData,"ON");
                CV.function=CMLib.xml().getIntFromPieces(voteData,"FUNC");
                CV.voteStatus=CMLib.xml().getIntFromPieces(voteData,"STATUS");
                CV.matter=CMLib.xml().getValFromPieces(voteData,"CMD");
                CV.votes=new DVector(2);
                Vector xV=CMLib.xml().getRealContentsFromPieces(voteData,"VOTES");
                if((xV!=null)&&(xV.size()>0))
                {
                    for(int x=0;x<xV.size();x++)
                    {
                        XMLLibrary.XMLpiece iblk=(XMLLibrary.XMLpiece)xV.elementAt(x);
                        if((!iblk.tag.equalsIgnoreCase("VOTE"))||(iblk.contents==null))
                            continue;
                        String userID=CMLib.xml().getValFromPieces(iblk.contents,"BY");
                        boolean yn=CMLib.xml().getBoolFromPieces(iblk.contents,"YN");
                        CV.votes.addElement(userID,new Boolean(yn));
                    }
                }
                voteList.addElement(CV);
            }
        }
        return voteList.elements();
    }

    public long getExp(){return exp;}
    public void setExp(long newexp){exp=newexp;}
    public void adjExp(int howMuch)
    {
        exp=exp+howMuch;
        if(exp<0) exp=0;
    }

    public int getTrophies(){return clanTrophies;}
    public void setTrophies(int trophyFlag){clanTrophies=trophyFlag;}

    public void setTaxes(double rate)
    {
        taxRate=rate;
    }
    public double getTaxes(){return taxRate;}
    
    public int getClanRelations(String id)
    {
        long i[]=(long[])relations.get(id.toUpperCase());
        if(i!=null) return (int)i[0];
        return  REL_NEUTRAL;
    }

    public long getLastRelationChange(String id)
    {
        long i[]=(long[])relations.get(id.toUpperCase());
        if(i!=null) return i[1];
        return 0;
    }
    public void setClanRelations(String id, int rel, long time)
    {
        relations.remove(id.toUpperCase());
        long[] i=new long[2];
        i[0]=rel;
        i[1]=time;
        relations.put(id.toUpperCase(),i);
    }

    public int getGovernment(){return government;}
    public void setGovernment(int type){government=type;}

    public void create()
    {
        CMLib.database().DBCreateClan(this);
        CMLib.clans().addClan(this);
    }

    public void update()
    {
        CMLib.database().DBUpdateClan(this);
    }

    public boolean updateClanPrivileges(MOB M)
    {
        boolean did=false;
        if(M==null) return false;
        if(M.getClanID().equals(ID())
        &&(M.getClanRole()!=POS_APPLICANT))
        {
            if(M.fetchAbility("Spell_ClanHome")==null)
            {
                M.addAbility(CMClass.findAbility("Spell_ClanHome"));
                (M.fetchAbility("Spell_ClanHome")).setProfficiency(50);
                did=true;
            }
            if(M.fetchAbility("Spell_ClanDonate")==null)
            {
                M.addAbility(CMClass.findAbility("Spell_ClanDonate"));
                (M.fetchAbility("Spell_ClanDonate")).setProfficiency(100);
                did=true;
            }
        }
        else
        {
            if(M.fetchAbility("Spell_ClanHome")!=null)
            {
                did=true;
                M.delAbility(M.fetchAbility("Spell_ClanHome"));
            }
            if(M.fetchAbility("Spell_ClanDonate")!=null)
            {
                did=true;
                M.delAbility(M.fetchAbility("Spell_ClanDonate"));
            }
        }
        if(((M.getClanID().equals(ID())))
        &&(allowedToDoThis(M,FUNC_CLANCANORDERCONQUERED)>0))
        {
            if(M.fetchAbility("Spell_Flagportation")==null)
            {
                M.addAbility(CMClass.findAbility("Spell_Flagportation"));
                (M.fetchAbility("Spell_Flagportation")).setProfficiency(100);
                did=true;
            }
        }
        else
        if(M.fetchAbility("Spell_Flagportation")!=null)
        {
            did=true;
            M.delAbility(M.fetchAbility("Spell_Flagportation"));
        }
        
        if(M.playerStats()!=null)
        for(int i=0;i<POSORDER.length;i++)
        {
            int pos=POSORDER[i];
            String title="*, "+CMLib.clans().getRoleName(getGovernment(),pos,true,false)+" of "+name();
            if((M.getClanRole()==pos)
            &&(M.getClanID().equals(ID()))
            &&(pos!=POS_APPLICANT))
            {
                if(!M.playerStats().getTitles().contains(title))
                    M.playerStats().getTitles().addElement(title);
            }
            else
            if(M.playerStats().getTitles().contains(title))
                M.playerStats().getTitles().remove(title);
        }
        if(M.getClanID().length()==0)
        {
            Item I=null;
            Vector itemsToMove=new Vector();
            for(int i=0;i<M.inventorySize();i++)
            {
                I=M.fetchInventory(i);
                if(I instanceof ClanItem)
                    itemsToMove.addElement(I);
            }
            for(int i=0;i<itemsToMove.size();i++)
            {
                I=(Item)itemsToMove.elementAt(i);
                Room R=null;
                if((getDonation()!=null)
                &&(getDonation().length()>0))
                    R=CMLib.map().getRoom(getDonation());
                if((R==null)
                &&(getRecall()!=null)
                &&(getRecall().length()>0))
                    R=CMLib.map().getRoom(getRecall());
                if(I instanceof Container)
                {
                    Vector V=((Container)I).getContents();
                    for(int v=0;v<V.size();v++)
                        ((Item)V.elementAt(v)).setContainer(null);
                }
                I.setContainer(null);
                I.wearAt(Item.INVENTORY);
                if(R!=null)
                    R.bringItemHere(I,0);
                else
                if(M.isMine(I))
                    I.destroy();
                did=true;
            }
        }
        if((did)&&(!CMSecurity.isSaveFlag("NOPLAYERS")))
            CMLib.database().DBUpdatePlayer(M);
        return did;
    }

    public void destroyClan()
    {
        DVector members=getMemberList();
        for(int m=0;m<members.size();m++)
        {
            String member=(String)members.elementAt(m,1);
            MOB M=CMLib.map().getLoadPlayer(member);
            if(M!=null)
            {
                M.setClanID("");
                M.setClanRole(0);
                updateClanPrivileges(M);
                CMLib.database().DBUpdateClanMembership(M.Name(), "", 0);
            }
        }
        CMLib.database().DBDeleteClan(this);
        CMLib.clans().removeClan(this);
    }

    public String getDetail(MOB mob)
    {
        StringBuffer msg=new StringBuffer("");
        msg.append("^x"+typeName()+" Profile   :^.^N "+ID()+"\n\r"
                  +"-----------------------------------------------------------------\n\r"
                  +getPremise()+"\n\r"
                  +"-----------------------------------------------------------------\n\r"
                  +"^xType            :^.^N "+Util.capitalizeAndLower(GVT_DESCS[getGovernment()])+"\n\r"
                  +"^xQualifications  :^.^N "+((getAcceptanceSettings().length()==0)?"Anyone may apply":CMLib.masking().maskDesc(getAcceptanceSettings()))+"\n\r");
        msg.append("^xExp. Tax Rate   :^.^N "+((int)Math.round(getTaxes()*100))+"%\n\r");
        if((mob.getClanID().equalsIgnoreCase(ID()))
        ||(Util.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS)))
        {
            msg.append("^xExperience Pts. :^.^N "+getExp()+"\n\r");
            if(getMorgue().length()>0)
            {
                Room R=CMLib.map().getRoom(getMorgue());
                if(R!=null)
                    msg.append("^x Morgue         :^.^N "+R.displayText()+"\n\r");
            }
            if(getRecall().length()>0)
            {
                Room R=CMLib.map().getRoom(getRecall());
                if(R!=null)
                    msg.append("^x Morgue         :^.^N "+R.displayText()+"\n\r");
            }
            msg.append("^xExperience Pts. :^.^N "+getExp()+"\n\r");
        }
        msg.append("^x"+Util.padRight(CMLib.clans().getRoleName(getGovernment(),POS_BOSS,true,true),16)+":^.^N "+crewList(POS_BOSS)+"\n\r"
                  +"^x"+Util.padRight(CMLib.clans().getRoleName(getGovernment(),POS_LEADER,true,true),16)+":^.^N "+crewList(POS_LEADER)+"\n\r"
                  +"^x"+Util.padRight(CMLib.clans().getRoleName(getGovernment(),POS_TREASURER,true,true),16)+":^.^N "+crewList(POS_TREASURER)+"\n\r"
                  +"^x"+Util.padRight(CMLib.clans().getRoleName(getGovernment(),POS_ENCHANTER,true,true),16)+":^.^N "+crewList(POS_ENCHANTER)+"\n\r"
                  +"^x"+Util.padRight(CMLib.clans().getRoleName(getGovernment(),POS_STAFF,true,true),16)+":^.^N "+crewList(POS_STAFF)+"\n\r"
                  +"^xTotal Members   :^.^N "+getSize()+"\n\r");
        if(CMLib.clans().numClans()>1)
        {
            msg.append("-----------------------------------------------------------------\n\r");
            msg.append("^x"+Util.padRight("Clan Relations",16)+":^.^N \n\r");
            for(Enumeration e=CMLib.clans().allClans();e.hasMoreElements();)
            {
                Clan C=(Clan)e.nextElement();
                if(C!=this)
                {
                    msg.append("^x"+Util.padRight(C.name(),16)+":^.^N ");
                    msg.append(Util.capitalizeAndLower(REL_DESCS[getClanRelations(C.clanID())]));
                    int orel=C.getClanRelations(ID());
                    if(orel!=REL_NEUTRAL)
                        msg.append(" (<-"+Util.capitalizeAndLower(REL_DESCS[orel])+")");
                    msg.append("\n\r");
                }
            }
        }
        if((mob.getClanID().equalsIgnoreCase(ID()))
        ||(Util.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS)))
        {
            if(mob.getClanID().equalsIgnoreCase(ID()))
                updateClanPrivileges(mob);
            msg.append("-----------------------------------------------------------------\n\r"
                      +"^x"+Util.padRight(CMLib.clans().getRoleName(getGovernment(),POS_MEMBER,true,true),16)
                      +":^.^N "+crewList(POS_MEMBER)+"\n\r");
            if((allowedToDoThis(mob,FUNC_CLANACCEPT)>=0)
            ||(Util.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS)))
            {
                msg.append("-----------------------------------------------------------------\n\r"
                        +"^x"+Util.padRight(CMLib.clans().getRoleName(getGovernment(),POS_APPLICANT,true,true),16)+":^.^N "+crewList(POS_APPLICANT)+"\n\r");
            }
        }
        Vector control=new Vector();
        Vector controlledAreas=getControlledAreas();
        long controlPoints=calculateMapPoints(controlledAreas);
        for(Enumeration e=controlledAreas.elements();e.hasMoreElements();)
            control.addElement(((Area)e.nextElement()).name());
        if(control.size()>0)
        {
            msg.append("-----------------------------------------------------------------\n\r");
            msg.append("^xClan Controlled Areas:^.^N\n\r");
            Collections.sort(control);
            int col=0;
            for(int i=0;i<control.size();i++)
            {
                if((++col)>3)
                {
                    msg.append("\n\r");
                    col=1;
                }
                msg.append(Util.padRight((String)control.elementAt(i),23)+"^N");
            }
            msg.append("\n\r");
        }
        if((CMLib.clans().trophySystemActive())&&(getTrophies()!=0))
        {
            msg.append("-----------------------------------------------------------------\n\r");
            msg.append("^xTrophies awarded:^.^N\n\r");
            for(int i=0;i<TROPHY_DESCS.length;i++)
                if((TROPHY_DESCS[i].length()>0)&&(Util.bset(getTrophies(),i)))
                {
                    msg.append(TROPHY_DESCS[i]+" ");
                    switch(i){
                        case TROPHY_AREA: msg.append("("+control.size()+") "); break;
                        case TROPHY_CONTROL: msg.append("("+controlPoints+") "); break;
                        case TROPHY_EXP: msg.append("("+getExp()+") "); break;
                    }
                    msg.append(" Prize: "+CMLib.clans().translatePrize(i)+"\n\r");
                }
        }
        return msg.toString();
    }

    private String crewList(int posType)
    {
        StringBuffer list=new StringBuffer("");
        DVector Members = getMemberList(posType);
        if(Members.size()>1)
        {
            for(int j=0;j<(Members.size() - 1);j++)
            {
                list.append(Members.elementAt(j,1)+", ");
            }
            list.append("and "+Members.elementAt(Members.size()-1,1));
        }
        else
        if(Members.size()>0)
        {
            list.append((String)Members.elementAt(0,1));
        }
        return list.toString();
    }

    public String typeName()
    {
        switch(clanType)
        {
        case TYPE_CLAN:
            if((getGovernment()>=0)&&(getGovernment()<GVT_DESCS.length))
                return Util.capitalizeAndLower(GVT_DESCS[getGovernment()].toLowerCase());
        }
        return "Clan";
    }

    public int allowedToDoThis(MOB mob, int function)
    {
        if(mob==null) return -1;
        int role=mob.getClanRole();
        if(role==POS_APPLICANT) return -1;
        if(government==GVT_OLIGARCHY)
        {
            switch(function)
            {
            case FUNC_CLANACCEPT:
                return ((role==POS_BOSS)||(role==POS_LEADER))?1:-1;
            case FUNC_CLANASSIGN:
                return 0;
            case FUNC_CLANEXILE:
                return ((role==POS_BOSS)||(role==POS_LEADER))?1:-1;
            case FUNC_CLANHOMESET:
                return 0;
            case FUNC_CLANTAX:
                return 0;
            case FUNC_CLANDONATESET:
                return 0;
            case FUNC_CLANREJECT:
                return (role==POS_BOSS)?1:-1;
            case FUNC_CLANPREMISE:
                return 0;
            case FUNC_CLANDECLARE:
                return 0;
            case FUNC_CLANPROPERTYOWNER:
                return ((role==POS_BOSS)||(role==POS_LEADER))?1:-1;
            case FUNC_CLANENCHANT:
                return ((role==POS_BOSS)||(role==POS_ENCHANTER))?1:-1;
            case FUNC_CLANWITHDRAW:
                return ((role==POS_BOSS)||(role==POS_TREASURER))?1:-1;
            case FUNC_CLANDEPOSITLIST:
                return ((role==POS_BOSS)||(role==POS_TREASURER))?1:-1;
            case FUNC_CLANCANORDERUNDERLINGS:
                return ((role==POS_BOSS)||(role==POS_LEADER)||(role==POS_TREASURER)||(role==POS_ENCHANTER)||(role==POS_STAFF))?1:-1;
            case FUNC_CLANCANORDERCONQUERED:
                return ((role==POS_BOSS)||(role==POS_LEADER)||(role==POS_TREASURER)||(role==POS_ENCHANTER)||(role==POS_STAFF)||(role==POS_MEMBER))?1:-1;
            case FUNC_CLANVOTEASSIGN:
                return (role==POS_BOSS)?1:-1;
            case FUNC_CLANVOTEOTHER:
                return (role==POS_BOSS)?1:-1;
            }
        }
        else
        if(government==GVT_REPUBLIC)
        {
            switch(function)
            {
            case FUNC_CLANACCEPT:
                return 0;
            case FUNC_CLANASSIGN:
                return 0;
            case FUNC_CLANTAX:
                return 0;
            case FUNC_CLANEXILE:
                return 0;
            case FUNC_CLANHOMESET:
                return 0;
            case FUNC_CLANDONATESET:
                return 0;
            case FUNC_CLANREJECT:
                return 0;
            case FUNC_CLANDECLARE:
                return 0;
            case FUNC_CLANPREMISE:
                return 0;
            case FUNC_CLANENCHANT:
                return (role==POS_ENCHANTER)?1:-1;
            case FUNC_CLANPROPERTYOWNER:
                return (role==POS_LEADER)?1:-1;
            case FUNC_CLANWITHDRAW:
                return (role==POS_TREASURER)?1:-1;
            case FUNC_CLANDEPOSITLIST:
                return ((role==POS_BOSS)||(role==POS_ENCHANTER)||(role==POS_LEADER)||(role==POS_TREASURER)||(role==POS_STAFF)||(role==POS_MEMBER))?1:-1;
            case FUNC_CLANCANORDERUNDERLINGS:
                return -1;
            case FUNC_CLANCANORDERCONQUERED:
                return (role==POS_STAFF)?1:-1;
            case FUNC_CLANVOTEASSIGN:
                return ((role==POS_BOSS)||(role==POS_ENCHANTER)||(role==POS_LEADER)||(role==POS_TREASURER)||(role==POS_STAFF)||(role==POS_MEMBER))?1:-1;
            case FUNC_CLANVOTEOTHER:
                return (role==POS_BOSS)?1:-1;
            }
        }
        else
        if(government==GVT_DEMOCRACY)
        {
            switch(function)
            {
            case FUNC_CLANACCEPT:
                return 0;
            case FUNC_CLANASSIGN:
                return 0;
            case FUNC_CLANEXILE:
                return 0;
            case FUNC_CLANDECLARE:
                return 0;
            case FUNC_CLANHOMESET:
                return 0;
            case FUNC_CLANTAX:
                return 0;
            case FUNC_CLANDONATESET:
                return 0;
            case FUNC_CLANREJECT:
                return 0;
            case FUNC_CLANPREMISE:
                return 0;
            case FUNC_CLANENCHANT:
                return (role==POS_ENCHANTER)?1:-1;
            case FUNC_CLANPROPERTYOWNER:
                return (role==POS_LEADER)?1:-1;
            case FUNC_CLANWITHDRAW:
                return (role==POS_TREASURER)?1:-1;
            case FUNC_CLANDEPOSITLIST:
                return ((role==POS_BOSS)||(role==POS_ENCHANTER)||(role==POS_LEADER)||(role==POS_TREASURER)||(role==POS_STAFF)||(role==POS_MEMBER))?1:-1;
            case FUNC_CLANCANORDERUNDERLINGS:
                return -1;
            case FUNC_CLANCANORDERCONQUERED:
                return (role==POS_STAFF)?1:-1;
            case FUNC_CLANVOTEASSIGN:
                return ((role==POS_BOSS)||(role==POS_ENCHANTER)||(role==POS_LEADER)||(role==POS_TREASURER)||(role==POS_STAFF)||(role==POS_MEMBER))?1:-1;
            case FUNC_CLANVOTEOTHER:
                return ((role==POS_BOSS)||(role==POS_ENCHANTER)||(role==POS_LEADER)||(role==POS_TREASURER)||(role==POS_STAFF)||(role==POS_MEMBER))?1:-1;
            }
        }
        else
        //if(government==GVT_DICTATORSHIP) or badly formed..
        {
            switch(function)
            {
            case FUNC_CLANACCEPT:
                return ((role==POS_BOSS)||(role==POS_LEADER))?1:-1;
            case FUNC_CLANASSIGN:
                return (role==POS_BOSS)?1:-1;
            case FUNC_CLANEXILE:
                return (role==POS_BOSS)?1:-1;
            case FUNC_CLANHOMESET:
                return (role==POS_BOSS)?1:-1;
            case FUNC_CLANDECLARE:
                return (role==POS_BOSS)?1:-1;
            case FUNC_CLANTAX:
                return (role==POS_BOSS)?1:-1;
            case FUNC_CLANDONATESET:
                return (role==POS_BOSS)?1:-1;
            case FUNC_CLANREJECT:
                return ((role==POS_BOSS)||(role==POS_LEADER))?1:-1;
            case FUNC_CLANPREMISE:
                return (role==POS_BOSS)?1:-1;
            case FUNC_CLANPROPERTYOWNER:
                return (role==POS_BOSS)?1:-1;
            case FUNC_CLANENCHANT:
                return ((role==POS_BOSS)||(role==POS_ENCHANTER))?1:-1;
            case FUNC_CLANWITHDRAW:
                return ((role==POS_BOSS)||(role==POS_TREASURER))?1:-1;
            case FUNC_CLANDEPOSITLIST:
                return ((role==POS_BOSS)||(role==POS_TREASURER))?1:-1;
            case FUNC_CLANCANORDERUNDERLINGS:
                return ((role==POS_BOSS)||(role==POS_LEADER))?1:-1;
            case FUNC_CLANCANORDERCONQUERED:
                return ((role==POS_BOSS)||(role==POS_ENCHANTER)||(role==POS_LEADER)||(role==POS_STAFF)||(role==POS_TREASURER))?1:-1;
            case FUNC_CLANVOTEASSIGN:
                return -1;
            case FUNC_CLANVOTEOTHER:
                return -1;
            }
        }
        return -1;
    }

    public DVector getRealMemberList(int PosFilter)
    {
        DVector members=getMemberList(PosFilter);
        if(members==null) return null;
        for(int i=members.size()-1;i>=0;i--)
        {
            String member=(String)members.elementAt(i,1);
            if(CMLib.map().getPlayer(member)!=null)
                continue;
            if(CMLib.database().DBUserSearch(null,member))
                continue;
            members.removeElementAt(i);
        }
        return members;
    }

    public int getSize()
    {
        Vector members=new Vector();
        return getSize(members);
    }

    public int getSize(Vector members)
    {
        CMLib.database().DBClanFill(this.ID(), members, new Vector(), new Vector());
        return members.size();
    }

    public String name() {return clanName;}
    public String getName() {return clanName;}
    public String clanID() {return clanName;}
    public void setName(String newName) {clanName = newName; }
    public int getType() {return clanType;}

    public String getPremise() {return clanPremise;}
    public void setPremise(String newPremise){ clanPremise = newPremise;}

    public String getAcceptanceSettings() { return AcceptanceSettings; }
    public void setAcceptanceSettings(String newSettings) { AcceptanceSettings=newSettings; }

    public String getPolitics() {
        StringBuffer str=new StringBuffer("");
        str.append("<POLITICS>");
        str.append(CMLib.xml().convertXMLtoTag("GOVERNMENT",""+getGovernment()));
        str.append(CMLib.xml().convertXMLtoTag("TAXRATE",""+getTaxes()));
        str.append(CMLib.xml().convertXMLtoTag("EXP",""+getExp()));
        if(relations.size()==0)
            str.append("<RELATIONS/>");
        else
        {
            str.append("<RELATIONS>");
            for(Enumeration e=relations.keys();e.hasMoreElements();)
            {
                String key=(String)e.nextElement();
                str.append("<RELATION>");
                str.append(CMLib.xml().convertXMLtoTag("CLAN",key));
                long[] i=(long[])relations.get(key);
                str.append(CMLib.xml().convertXMLtoTag("STATUS",""+i[0]));
                str.append("</RELATION>");
            }
            str.append("</RELATIONS>");
        }
        str.append("</POLITICS>");
        return str.toString();
    }
    public void setPolitics(String politics)
    {
        relations.clear();
        government=GVT_DICTATORSHIP;
        if(politics.trim().length()==0) return;
        Vector xml=CMLib.xml().parseAllXML(politics);
        if(xml==null)
        {
            Log.errOut("Clans","Unable to parse: "+politics);
            return;
        }
        Vector poliData=CMLib.xml().getRealContentsFromPieces(xml,"POLITICS");
        if(poliData==null){ Log.errOut("Clans","Unable to get POLITICS data."); return;}
        government=CMLib.xml().getIntFromPieces(poliData,"GOVERNMENT");
        exp=CMLib.xml().getLongFromPieces(poliData,"EXP");
        taxRate=CMLib.xml().getDoubleFromPieces(poliData,"TAXRATE");

        // now RESOURCES!
        Vector xV=CMLib.xml().getRealContentsFromPieces(poliData,"RELATIONS");
        if((xV!=null)&&(xV.size()>0))
        {
            for(int x=0;x<xV.size();x++)
            {
                XMLLibrary.XMLpiece iblk=(XMLLibrary.XMLpiece)xV.elementAt(x);
                if((!iblk.tag.equalsIgnoreCase("RELATION"))||(iblk.contents==null))
                    continue;
                String relClanID=CMLib.xml().getValFromPieces(iblk.contents,"CLAN");
                int rel=CMLib.xml().getIntFromPieces(iblk.contents,"STATUS");
                setClanRelations(relClanID,rel,0);
            }
        }
    }

    public int getStatus() { return ClanStatus; }
    public void setStatus(int newStatus) { ClanStatus=newStatus; }

    public String getRecall() { return clanRecall; }
    public void setRecall(String newRecall) { clanRecall=newRecall; }

    public String getMorgue() { return clanMorgue; }
    public void setMorgue(String newMorgue) { clanMorgue=newMorgue; }

    public String getDonation() { return clanDonationRoom; }
    public void setDonation(String newDonation) { clanDonationRoom=newDonation; }

    public DVector getMemberList()
    {
        return getMemberList(-1);
    }

    public DVector getMemberList(int PosFilter)
    {
        DVector filteredMembers=new DVector(3);
        Vector members=new Vector();
        Vector roles=new Vector();
        Vector lastDates=new Vector();
        CMLib.database().DBClanFill(this.ID(), members, roles, lastDates);
        for(int s=0;s<members.size();s++)
        {
            int posFilter=((Integer)roles.elementAt(s)).intValue();
            if((posFilter==PosFilter)||(PosFilter<0))
                filteredMembers.addElement(members.elementAt(s),roles.elementAt(s),lastDates.elementAt(s));
        }
        return filteredMembers;
    }

    public int getNumVoters(int function)
    {
        int realmembers=0;
        int bosses=0;
        DVector members=getMemberList();
        for(int m=0;m<members.size();m++)
        {
            if(((Integer)members.elementAt(m,2)).intValue()==POS_BOSS)
            {
                realmembers++;
                bosses++;
            }
            else
            if(((Integer)members.elementAt(m,2)).intValue()!=POS_APPLICANT)
                realmembers++;
        }
        int numVotes=bosses;
        if(getGovernment()==GVT_DEMOCRACY)
            numVotes=realmembers;
        else
        if((getGovernment()==GVT_REPUBLIC)&&(function==FUNC_CLANASSIGN))
            numVotes=realmembers;
        return numVotes;
    }


    public int getTopRank() {
        if((getGovernment()>=0)
        &&(getGovernment()<topRanks.length))
            return topRanks[getGovernment()];
        return POS_BOSS;
    }

    public boolean tick(Tickable ticking, int tickID)
    {
        if(tickID!=MudHost.TICK_CLAN)
            return true;
        try{
            DVector members=getMemberList();
            int activeMembers=0;
            long deathMilis=CMProps.getIntVar(CMProps.SYSTEMI_DAYSCLANDEATH)*CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDDAY)*MudHost.TICK_TIME;
            int[] numTypes=new int[POSORDER.length];
            for(int j=0;j<members.size();j++)
            {
                long lastLogin=((Long)members.elementAt(j,3)).longValue();
                if(((System.currentTimeMillis()-lastLogin)<deathMilis)||(deathMilis==0))
                    activeMembers++;
                numTypes[CMLib.clans().getRoleOrder(((Integer)members.elementAt(j,2)).intValue())]++;
            }

            // handle any necessary promotions
            if((getGovernment()==GVT_DICTATORSHIP)
            ||(getGovernment()==GVT_OLIGARCHY))
            {
                int highest=0;
                for(int i=numTypes.length-1;i>=0;i--)
                    if(numTypes[i]>0){ highest=i; break;}
                int max=topRanks[getGovernment()];
                if(highest<CMLib.clans().getRoleOrder(max))
                {
                    for(int i=0;i<members.size();i++)
                    {
                        if(CMLib.clans().getRoleOrder(((Integer)members.elementAt(i,2)).intValue())==highest)
                        {
                            String s=(String)members.elementAt(i,1);
                            MOB M2=CMLib.map().getLoadPlayer(s);
                            if(M2!=null) 
                            {
                                clanAnnounce(s+" is now a "+CMLib.clans().getRoleName(getGovernment(),max,true,false)+" of the "+typeName()+" "+name()+".");
                                Log.sysOut("Clans",s+" of clan "+name()+" was autopromoted to "+CMLib.clans().getRoleName(getGovernment(),max,true,false)+".");
                                M2.setClanRole(max);
                                CMLib.database().DBUpdateClanMembership(s, ID(), max);
                            }
                            break;
                        }
                    }
                }
            }


            if(activeMembers<CMProps.getIntVar(CMProps.SYSTEMI_MINCLANMEMBERS))
            {
                if(getStatus()==CLANSTATUS_FADING)
                {
                    Log.sysOut("Clans","Clan '"+getName()+" deleted with only "+activeMembers+" having logged on lately.");
                    destroyClan();
                    StringBuffer buf=new StringBuffer("");
                    for(int j=0;j<members.size();j++)
                    {
                        String s=(String)members.elementAt(j,1);
                        long lastLogin=((Long)members.elementAt(j,3)).longValue();
                        buf.append(s+" on "+CMLib.time().date2String(lastLogin)+"  ");
                    }
                    Log.sysOut("Clans","Clan '"+getName()+" had the following membership: "+buf.toString());
                    return true;
                }
                setStatus(CLANSTATUS_FADING);
                Log.sysOut("Clans","Clan '"+getName()+" fading with only "+activeMembers+" having logged on lately.");
                clanAnnounce(""+typeName()+" "+name()+" is in danger of being deleted if more members do not log on within 24 hours.");
            }
            else
            switch(getStatus())
            {
            case CLANSTATUS_FADING:
                setStatus(CLANSTATUS_ACTIVE);
                clanAnnounce(""+typeName()+" "+name()+" is no longer in danger of being deleted.  Be aware that there is required activity level.");
                break;
            case CLANSTATUS_PENDING:
                setStatus(CLANSTATUS_ACTIVE);
                Log.sysOut("Clans",""+typeName()+" '"+getName()+" now active with "+activeMembers+".");
                clanAnnounce(""+typeName()+" "+name()+" now has sufficient members.  The "+typeName()+" is now fully approved.");
                break;
            default:
                break;
            }


            // now do votes
            if((getGovernment()!=GVT_DICTATORSHIP)&&(votes()!=null))
            {
                boolean updateVotes=false;
                Vector votesToRemove=new Vector();
                Vector data=null;
                switch(getGovernment())
                {
                case GVT_DEMOCRACY:
                    data=Util.parseCommas(CMProps.getVar(CMProps.SYSTEM_CLANVOTED),false);
                    break;
                case GVT_OLIGARCHY:
                    data=Util.parseCommas(CMProps.getVar(CMProps.SYSTEM_CLANVOTEO),false);
                    break;
                case GVT_REPUBLIC:
                    data=Util.parseCommas(CMProps.getVar(CMProps.SYSTEM_CLANVOTER),false);
                    break;
                default:
                    data=new Vector();
                    break;
                }
                long duration=54;
                if(data.size()>0) duration=Util.s_long((String)data.firstElement());
                if(duration<=0) duration=54;
                duration=duration*CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDDAY)*MudHost.TICK_TIME;
                for(Enumeration e=votes();e.hasMoreElements();)
                {
                    ClanVote CV=(ClanVote)e.nextElement();
                    int numVotes=getNumVoters(CV.function);
                    int quorum=50;
                    if(data.size()>1) quorum=Util.s_int((String)data.lastElement());
                    quorum=(int)Math.round(Util.mul(Util.div(quorum,100.0),numVotes));
                    if(quorum<2) quorum=2;
                    if(numVotes==1) quorum=1;
                    long endsOn=CV.voteStarted+duration;
                    if(CV.voteStatus==VSTAT_STARTED)
                    {
                        if(CV.votes==null) CV.votes=new DVector(2);
                        boolean voteIsOver=false;
                        if(System.currentTimeMillis()>endsOn)
                            voteIsOver=true;
                        else
                        if(CV.votes.size()==numVotes)
                            voteIsOver=true;
                        if(voteIsOver)
                        {
                            CV.voteStarted=System.currentTimeMillis();
                            updateVotes=true;
                            if(CV.votes.size()<quorum)
                                CV.voteStatus=VSTAT_FAILED;
                            else
                            {
                                int yeas=0;
                                int nays=0;
                                for(int i=0;i<CV.votes.size();i++)
                                    if(((Boolean)CV.votes.elementAt(i,2)).booleanValue())
                                        yeas++;
                                    else
                                        nays++;
                                if(yeas<=nays)
                                    CV.voteStatus=VSTAT_FAILED;
                                else
                                {
                                    CV.voteStatus=VSTAT_PASSED;
                                    MOB mob=CMClass.getMOB("StdMOB");
                                    mob.setName(ID());
                                    mob.setClanID(ID());
                                    mob.setClanRole(POS_BOSS);
                                    mob.baseEnvStats().setLevel(1000);
                                    if(mob.location()==null)
                                    {
                                        mob.setLocation(mob.getStartRoom());
                                        if(mob.location()==null)
                                            mob.setLocation(CMLib.map().getRandomRoom());
                                    }
                                    Vector V=Util.parse(CV.matter);
                                    mob.doCommand(V);
                                    mob.destroy();
                                }
                            }
                        }
                    }
                    else
                    if(System.currentTimeMillis()>endsOn)
                    {
                        updateVotes=true;
                        votesToRemove.addElement(CV);
                    }
                }
                for(int v=0;v<votesToRemove.size();v++)
                    delVote(votesToRemove.elementAt(v));
                if(updateVotes)
                    updateVotes();
            }
            
            if(CMLib.clans().trophySystemActive())
            {
                // calculate winner of the exp contest
                if(CMProps.getVar(CMProps.SYSTEM_CLANTROPEXP).length()>0)
                {
                    Clan winner=null;
                    for(Enumeration e=CMLib.clans().clans();e.hasMoreElements();)
                    {
                        Clan C=(Clan)e.nextElement();
                        if((winner==null)||(C.getExp()>winner.getExp()))
                            winner=C;
                    }
                    if(winner==this)
                    {
                        if((!Util.bset(getTrophies(),TROPHY_EXP))&&(getExp()>0))
                        {
                            setTrophies(getTrophies()|TROPHY_EXP);
                            CMLib.clans().clanAnnounceAll("The "+typeName()+" "+name()+" has been awarded the trophy for "+TROPHY_DESCS[TROPHY_EXP]+".");
                        }
                    }
                    else
                    if(Util.bset(getTrophies(),TROPHY_EXP))
                    {
                        setTrophies(getTrophies()-TROPHY_EXP);
                        clanAnnounce("The "+typeName()+" "+name()+" has lost control of the trophy for "+TROPHY_DESCS[TROPHY_EXP]+".");
                    }
                }
                
                // calculate winner of the pk contest
                if(CMProps.getVar(CMProps.SYSTEM_CLANTROPPK).length()>0)
                {
                    Clan winner=null;
                    for(Enumeration e=CMLib.clans().clans();e.hasMoreElements();)
                    {
                        Clan C=(Clan)e.nextElement();
                        if((winner==null)||(C.getCurrentClanKills()>winner.getCurrentClanKills()))
                            winner=C;
                    }
                    if(winner==this)
                    {
                        if((!Util.bset(getTrophies(),TROPHY_PK))
                        &&(getCurrentClanKills()>0))
                        {
                            setTrophies(getTrophies()|TROPHY_PK);
                            CMLib.clans().clanAnnounceAll("The "+typeName()+" "+name()+" has been awarded the trophy for "+TROPHY_DESCS[TROPHY_PK]+".");
                        }
                    }
                    else
                    if(Util.bset(getTrophies(),TROPHY_PK))
                    {
                        setTrophies(getTrophies()-TROPHY_PK);
                        clanAnnounce("The "+typeName()+" "+name()+" has lost control of the trophy for "+TROPHY_DESCS[TROPHY_PK]+".");
                    }
                }
                
                // calculate winner of the conquest contests
                if((CMProps.getVar(CMProps.SYSTEM_CLANTROPAREA).length()>0)
                ||(CMProps.getVar(CMProps.SYSTEM_CLANTROPCP).length()>0))
                {
                    long mostClansControlled=-1;
                    Clan winnerMostClansControlled=null;
                    long mostControlPoints=-1;
                    Clan winnerMostControlPoints=null;
                    Vector tempControl=null;
                    long tempNumber=0;
                    for(Enumeration e=CMLib.clans().clans();e.hasMoreElements();)
                    {
                        Clan C=(Clan)e.nextElement();
                        tempControl=C.getControlledAreas();
                        tempNumber=C.calculateMapPoints(tempControl);
                        if((winnerMostClansControlled==null)||(tempControl.size()>mostClansControlled))
                        {
                            winnerMostClansControlled=C;
                            mostClansControlled=tempControl.size();
                        }
                        if((winnerMostControlPoints==null)||(tempNumber>mostControlPoints))
                        {
                            winnerMostControlPoints=C;
                            mostControlPoints=tempNumber;
                        }
                    }
                    if((winnerMostClansControlled==this)
                    &&(CMProps.getVar(CMProps.SYSTEM_CLANTROPAREA).length()>0)
                    &&(mostClansControlled>0))
                    {
                        if(!Util.bset(getTrophies(),TROPHY_AREA))
                        {
                            setTrophies(getTrophies()|TROPHY_AREA);
                            CMLib.clans().clanAnnounceAll("The "+typeName()+" "+name()+" has been awarded the trophy for "+TROPHY_DESCS[TROPHY_AREA]+".");
                        }
                    }
                    else
                    if(Util.bset(getTrophies(),TROPHY_AREA))
                    {
                        setTrophies(getTrophies()-TROPHY_AREA);
                        clanAnnounce("The "+typeName()+" "+name()+" has lost control of the trophy for "+TROPHY_DESCS[TROPHY_AREA]+".");
                    }
                    if((winnerMostControlPoints==this)
                    &&(CMProps.getVar(CMProps.SYSTEM_CLANTROPCP).length()>0)
                    &&(mostControlPoints>0))
                    {
                        if(!Util.bset(getTrophies(),TROPHY_CONTROL))
                        {
                            setTrophies(getTrophies()|TROPHY_CONTROL);
                            CMLib.clans().clanAnnounceAll("The "+typeName()+" "+name()+" has been awarded the trophy for "+TROPHY_DESCS[TROPHY_CONTROL]+".");
                        }
                    }
                    else
                    if(Util.bset(getTrophies(),TROPHY_CONTROL))
                    {
                        setTrophies(getTrophies()-TROPHY_CONTROL);
                        clanAnnounce("The "+typeName()+" "+name()+" has lost control of the trophy for "+TROPHY_DESCS[TROPHY_CONTROL]+".");
                    }
                }
            }
            update(); // also saves exp, and trophies
        }
        catch(Exception x2)
        {
            Log.errOut("Clans",x2);
        }
        return true;
    }

    public void clanAnnounce(String msg)
    {
        Vector channels=CMLib.channels().getFlaggedChannelNames("CLANINFO");
        for(int i=0;i<channels.size();i++)
            CMLib.commands().channel((String)channels.elementAt(i),ID(),msg,true);
    }

    public int applyExpMods(int exp)
    {
        boolean changed=false;
        if((getTaxes()>0.0)&&(exp>1))
        {
            int clanshare=(int)Math.round(Util.mul(exp,getTaxes()));
            if(clanshare>0)
            {
                exp-=clanshare;
                adjExp(clanshare);
                changed=true;
            }
        }
        for(int i=0;i<TROPHY_DESCS_SHORT.length;i++)
            if((TROPHY_DESCS_SHORT[i].length()>0)
            &&(Util.bset(getTrophies(),i)))
            {
                String awardStr=null;
                switch(i)
                {
                case TROPHY_AREA: awardStr=CMProps.getVar(CMProps.SYSTEM_CLANTROPAREA); break;
                case TROPHY_CONTROL: awardStr=CMProps.getVar(CMProps.SYSTEM_CLANTROPCP); break;
                case TROPHY_EXP: awardStr=CMProps.getVar(CMProps.SYSTEM_CLANTROPEXP); break;
                default: awardStr=null;
                }
                if(awardStr!=null)
                {
                    int amount=0;
                    double pct=0.0;
                    Vector V=Util.parse(awardStr);
                    if(V.size()>=2)
                    {
                        String type=((String)V.lastElement()).toUpperCase();
                        String amt=(String)V.firstElement();
                        if(amt.endsWith("%"))
                            pct=Util.div(Util.s_int(amt.substring(0,amt.length()-1)),100.0);
                        else
                            amount=Util.s_int(amt);
                        if("EXPERIENCE".startsWith(type))
                            exp+=((int)Math.round(Util.mul(exp,pct)))+amount;
                    }
                }
            }
        if(changed) update();
        return exp;
    }
    public MOB getResponsibleMember()
    {
        MOB mob=null;
        DVector DV=getMemberList();
        int newPos=-1;
        for(int i=0;i<DV.size();i++)
            if(((Integer)DV.elementAt(i,2)).intValue()>newPos)
            {    
                mob=CMLib.map().getLoadPlayer((String)DV.elementAt(i,1));
                if(mob!=null)
                    break;
            }
        return mob;
    }

    
}
