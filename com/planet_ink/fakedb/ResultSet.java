package com.planet_ink.fakedb;

import java.io.InputStream;
import java.io.Reader;
import java.sql.NClob;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.util.Map;
/* 
   Copyright 2001 Thomas Neumann

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
@SuppressWarnings("unchecked")
public class ResultSet implements java.sql.ResultSet
{
   public static class FakeCondition
   {
	   public int conditionIndex;
	   public String conditionValue;
	   public boolean eq=true;
	   public boolean lt=false;
	   public boolean gt=false;
   }
   
   
   private Statement statement;
   private Backend.FakeTable relation;
   private java.util.Iterator iter;
   private int currentRow=0;
   private int conditionIndex;
   private String conditionValue;
   private boolean eq=true;
   private boolean lt=false;
   private boolean gt=false;
   private final String[] values;
   private final boolean[] nullIndicators;
   private boolean nullFlag = false;

   ResultSet(Statement s,
             Backend.FakeTable r,
             int ci,
             String cv,
             String comp) 
   {
      statement=s;
      relation=r;
      conditionIndex=ci;
      conditionValue=cv;
      comp=comp.trim();
      eq=(comp.indexOf("=")>=0);
      lt=(comp.indexOf("<")>=0);
      gt=(comp.indexOf(">")>=0) ;
	  currentRow=0;
      values=new String[r.columns.length];
      nullIndicators=new boolean[values.length];

      if ((ci<0)&&(cv!=null)) {
         iter=r.index.keySet().iterator();
      } else {
         iter=r.index.values().iterator();
      }
   }

   public java.sql.Statement getStatement() throws java.sql.SQLException { return statement; }

    public static boolean isNumber(String s)
    {
        if(s==null) return false;
        s=s.trim();
        if(s.length()==0) return false;
        if((s.length()>1)&&(s.startsWith("-")))
            s=s.substring(1);
        for(int i=0;i<s.length();i++)
            if("0123456789.,".indexOf(s.charAt(i))<0)
                return false;
        return true;
    }
    
    public static double s_double(String DOUBLE)
    {
        double sdouble=0;
        try{ sdouble=Double.parseDouble(DOUBLE); }
        catch(Exception e){ return 0;}
        return sdouble;
    }
    
    public static long s_long(String LONG)
    {
        long slong=0;
        try{ slong=Long.parseLong(LONG); }
        catch(Exception e){ return 0;}
        return slong;
    }
    
    public static boolean isDouble(String DBL)
    {
        if(DBL.length()==0) return false;
        if(DBL.startsWith("-")&&(DBL.length()>1))
            DBL=DBL.substring(1);
        boolean alreadyDot=false;
        for(int i=0;i<DBL.length();i++)
            if(!Character.isDigit(DBL.charAt(i)))
            {
                if(DBL.charAt(i)=='.')
                {
                    if(alreadyDot)
                        return false;
                    alreadyDot=true;
                }
                else
                    return false;
            }
        return alreadyDot;
    }
    
    public int numCompare(String s1, String s2)
    {
        if((s1==null)||(s2==null)) return 0;
        if((!isNumber(s1))||(!isNumber(s2))) return 0;
        if(isDouble(s1)||(isDouble(s2)))
        {
            double d1=isDouble(s1)?s_double(s1):Long.valueOf(s_long(s1)).doubleValue();
            double d2=isDouble(s2)?s_double(s2):Long.valueOf(s_long(s2)).doubleValue();
            if(d1==d2) return 0;
            if(d1>d2) return 1;
            return -1;
        }
        long l1=s_long(s1);
        long l2=s_long(s2);
        if(l1==l2) return 0;
        if(l1>l2) return 1;
        return -1;
    }
    
   public boolean next() throws java.sql.SQLException
   {
      while (true) 
      {
         if (!iter.hasNext()) return false;
         if ((conditionIndex<0)&&(conditionValue!=null)) 
         {
             String key=(String)iter.next();
             String subKey=key;
             int x=subKey.indexOf("\n");
             if(x>0)subKey=subKey.substring(0,x);
             int nc=(lt||gt)?numCompare(subKey,conditionValue):0;
             int sc=(lt||gt)?subKey.compareTo(conditionValue):0;
             if(((eq)&&(subKey.equals(conditionValue)))
             ||((eq)&&(key.startsWith(conditionValue+"\n")))
             ||((lt)&&(nc<0))
             ||((gt)&&(nc>0))
             ||((lt)&&(sc<0))
             ||((gt)&&(sc>0)))
             {
                 currentRow++;
                 return relation.getRecord(nullIndicators, values, (Backend.RecordInfo)relation.index.get(key));
             }
             continue;
         }
        if (!relation.getRecord(nullIndicators,values,(Backend.RecordInfo)iter.next())) 
			return false;
        if (conditionIndex>=0) 
        {
           if (nullIndicators[conditionIndex]) 
               continue;
           String subKey=values[conditionIndex];
           int nc=(lt||gt)?numCompare(subKey,conditionValue):0;
           int sc=(lt||gt)?subKey.compareTo(conditionValue):0;
           if(!(((eq)&&(subKey.equals(conditionValue)))
           ||((lt)&&(nc<0))
           ||((gt)&&(nc>0))
           ||((lt)&&(sc<0))
           ||((gt)&&(sc>0))))
               continue;
        }
		currentRow++;
        return true;
      }
   }
   public void close() throws java.sql.SQLException
   {
   }
   public boolean wasNull() throws java.sql.SQLException
   {
       return nullFlag;
   }
   public String getString(int columnIndex) throws java.sql.SQLException
   {
      if ((columnIndex<0)||(columnIndex>=nullIndicators.length)||(nullIndicators[columnIndex])) {
         nullFlag=true;
         return null;
      } 
      nullFlag=false;
      return values[columnIndex];
   }
   public java.sql.Array getArray(int columnIndex) throws java.sql.SQLException
   {
      //String s=getString(columnIndex);
      if (nullFlag) return null;
      throw new java.sql.SQLException();
   }
   public java.sql.Blob getBlob(int columnIndex) throws java.sql.SQLException
   {
      //String s=getString(columnIndex);
      if (nullFlag) return null;
      throw new java.sql.SQLException();
   }
   public java.sql.Clob getClob(int columnIndex) throws java.sql.SQLException
   {
      //String s=getString(columnIndex);
      if (nullFlag) return null;
      throw new java.sql.SQLException();
   }
   public java.sql.Ref getRef(int columnIndex) throws java.sql.SQLException
   {
      //String s=getString(columnIndex);
      if (nullFlag) return null;
      throw new java.sql.SQLException();
   }

   public boolean getBoolean(int columnIndex) throws java.sql.SQLException
   {
      String s=getString(columnIndex);
      if ((s!=null)&&(s.length()>0))
         switch (Character.toUpperCase(s.charAt(0))) {
            case 'T': case 'Y': case '1': return true;
         }
      return false;
   }
   public byte getByte(int columnIndex) throws java.sql.SQLException
   {
      String s=getString(columnIndex);
      if (nullFlag) return 0;
      try {
         return Byte.parseByte(s);
      } catch (NumberFormatException e) { throw new java.sql.SQLException(e.getMessage()); }
   }
   public short getShort(int columnIndex) throws java.sql.SQLException
   {
      String s=getString(columnIndex);
      if (nullFlag) return 0;
      try {
         return Short.parseShort(s);
      } catch (NumberFormatException e) { throw new java.sql.SQLException(e.getMessage()); }
   }
   public int getInt(int columnIndex) throws java.sql.SQLException
   {
      String s=getString(columnIndex);
      if (nullFlag) return 0;
      try {
         return Integer.parseInt(s);
      } catch (NumberFormatException e) { throw new java.sql.SQLException(e.getMessage()); }
   }
   public long getLong(int columnIndex) throws java.sql.SQLException
   {
      String s=getString(columnIndex);
      if (nullFlag) return 0;
      try {
         return Long.parseLong(s);
      } catch (NumberFormatException e) { throw new java.sql.SQLException(e.getMessage()); }
   }
   public float getFloat(int columnIndex) throws java.sql.SQLException
   {
      String s=getString(columnIndex);
      if (nullFlag) return 0;
      try {
         return Float.parseFloat(s);
      } catch (NumberFormatException e) { throw new java.sql.SQLException(e.getMessage()); }
   }
   public double getDouble(int columnIndex) throws java.sql.SQLException
   {
      String s=getString(columnIndex);
      if (nullFlag) return 0;
      try {
         return Double.parseDouble(s);
      } catch (NumberFormatException e) { throw new java.sql.SQLException(e.getMessage()); }
   }
   public java.math.BigDecimal getBigDecimal(int columnIndex) throws java.sql.SQLException
   {
      String s=getString(columnIndex);
      if (nullFlag) return new java.math.BigDecimal(0);
      try {
         return new java.math.BigDecimal(s);
      } catch (NumberFormatException e) { throw new java.sql.SQLException(e.getMessage()); }
   }
   /**
    * @deprecated
    */
   public java.math.BigDecimal getBigDecimal(int columnIndex, int scale) throws java.sql.SQLException
   {
      String s=getString(columnIndex);
      if (nullFlag) { java.math.BigDecimal v=new java.math.BigDecimal(0); v.setScale(scale); return v; }
      try {
         java.math.BigDecimal v=new java.math.BigDecimal(s); v.setScale(scale); return v;
      } catch (NumberFormatException e) { throw new java.sql.SQLException(e.getMessage()); }
   }
   public byte[] getBytes(int columnIndex) throws java.sql.SQLException
   {
      String s=getString(columnIndex);
      if (nullFlag) return null;
      try {
         return s.getBytes();
      } catch (NumberFormatException e) { throw new java.sql.SQLException(e.getMessage()); }
   }
   public java.sql.Date getDate(int columnIndex) throws java.sql.SQLException
   {
      String s=getString(columnIndex);
      if (nullFlag) return null;
      try {
         return java.sql.Date.valueOf(s);
      } catch (NumberFormatException e) { throw new java.sql.SQLException(e.getMessage()); }
   }
   public java.sql.Time getTime(int columnIndex) throws java.sql.SQLException
   {
      String s=getString(columnIndex);
      if (nullFlag) return null;
      try {
         return java.sql.Time.valueOf(s);
      } catch (NumberFormatException e) { throw new java.sql.SQLException(e.getMessage()); }
   }
   public java.sql.Timestamp getTimestamp(int columnIndex) throws java.sql.SQLException
   {
      String s=getString(columnIndex);
      if (nullFlag) return null;
      try {
         return java.sql.Timestamp.valueOf(s);
      } catch (NumberFormatException e) { throw new java.sql.SQLException(e.getMessage()); }
   }
   public java.io.InputStream getAsciiStream(int columnIndex) throws java.sql.SQLException
   {
      return getBinaryStream(columnIndex);
   }
   /**
    * @deprecated
    */
   public java.io.InputStream getUnicodeStream(int columnIndex) throws java.sql.SQLException
   {
      return getBinaryStream(columnIndex);
   }
   public java.io.InputStream getBinaryStream(int columnIndex) throws java.sql.SQLException
   {
      byte b[] = getBytes(columnIndex);
      if (nullFlag) return null;
      return new java.io.ByteArrayInputStream(b);
   }
   public java.io.Reader getCharacterStream(int columnIndex) throws java.sql.SQLException
   {
      String s=getString(columnIndex);
      if (nullFlag) return null;
      return new java.io.CharArrayReader(s.toCharArray());
   }
   public Object getObject(int columnIndex) throws java.sql.SQLException
   {
      return getString(columnIndex);
   }
   public java.net.URL getURL(int columnIndex) throws java.sql.SQLException
   {
      String s=getString(columnIndex);
      if (nullFlag) return null;
      try {
         return new java.net.URL(s);
      } catch (java.net.MalformedURLException e) { throw new java.sql.SQLException(e.getMessage()); }
   }

   public int findColumn(String columnName) throws java.sql.SQLException
   {
      return relation.findColumn(columnName);
   }



   public String getString(String columnName) throws java.sql.SQLException
      { return getString(findColumn(columnName)); }
   public java.sql.Array getArray(String columnName) throws java.sql.SQLException
      { return getArray(findColumn(columnName)); }
   public java.sql.Blob getBlob(String columnName) throws java.sql.SQLException
      { return getBlob(findColumn(columnName)); }
   public java.sql.Clob getClob(String columnName) throws java.sql.SQLException
      { return getClob(findColumn(columnName)); }
   public java.sql.Ref getRef(String columnName) throws java.sql.SQLException
      { return getRef(findColumn(columnName)); }
   public boolean getBoolean(String columnName) throws java.sql.SQLException
      { return getBoolean(findColumn(columnName)); }
   public byte getByte(String columnName) throws java.sql.SQLException
      { return getByte(findColumn(columnName)); }
   public short getShort(String columnName) throws java.sql.SQLException
      { return getShort(findColumn(columnName)); }
   public int getInt(String columnName) throws java.sql.SQLException
      { return getInt(findColumn(columnName)); }
   public long getLong(String columnName) throws java.sql.SQLException
      { return getLong(findColumn(columnName)); }
   public float getFloat(String columnName) throws java.sql.SQLException
      { return getFloat(findColumn(columnName)); }
   public double getDouble(String columnName) throws java.sql.SQLException
      { return getDouble(findColumn(columnName)); }
   public java.math.BigDecimal getBigDecimal(String columnName) throws java.sql.SQLException
      { return getBigDecimal(findColumn(columnName)); }
   /**
    * @deprecated
    */
   public java.math.BigDecimal getBigDecimal(String columnName, int scale) throws java.sql.SQLException
      { return getBigDecimal(findColumn(columnName), scale); }
   public byte[] getBytes(String columnName) throws java.sql.SQLException
      { return getBytes(findColumn(columnName)); }
   public java.sql.Date getDate(String columnName) throws java.sql.SQLException
      { return getDate(findColumn(columnName)); }
   public java.sql.Date getDate(int columnName,java.util.Calendar c) throws java.sql.SQLException
      { return getDate(columnName); }
   public java.sql.Date getDate(String columnName,java.util.Calendar c) throws java.sql.SQLException
      { return getDate(findColumn(columnName)); }
   public java.sql.Time getTime(String columnName) throws java.sql.SQLException
      { return getTime(findColumn(columnName)); }
   public java.sql.Time getTime(int columnName,java.util.Calendar c) throws java.sql.SQLException
      { return getTime(columnName); }
   public java.sql.Time getTime(String columnName,java.util.Calendar c) throws java.sql.SQLException
      { return getTime(findColumn(columnName)); }
   public java.sql.Timestamp getTimestamp(String columnName) throws java.sql.SQLException
      { return getTimestamp(findColumn(columnName)); }
   public java.sql.Timestamp getTimestamp(int columnName,java.util.Calendar c) throws java.sql.SQLException
      { return getTimestamp(columnName); }
   public java.sql.Timestamp getTimestamp(String columnName,java.util.Calendar c) throws java.sql.SQLException
      { return getTimestamp(findColumn(columnName)); }
   public java.io.Reader getCharacterStream(String columnName) throws java.sql.SQLException
      { return getCharacterStream(findColumn(columnName)); }
   public java.io.InputStream getAsciiStream(String columnName) throws java.sql.SQLException
      { return getAsciiStream(findColumn(columnName)); }
   /**
    * @deprecated
    */
   public java.io.InputStream getUnicodeStream(String columnName) throws java.sql.SQLException
      { return getUnicodeStream(findColumn(columnName)); }
   public java.io.InputStream getBinaryStream(String columnName) throws java.sql.SQLException
      { return getBinaryStream(findColumn(columnName)); }
   public java.net.URL getURL(String columnName) throws java.sql.SQLException
      { return getURL(findColumn(columnName)); }
   public Object getObject(String columnName) throws java.sql.SQLException
      { return getObject(findColumn(columnName)); }
   public java.sql.SQLWarning getWarnings() throws java.sql.SQLException
      { return null; }
   public void clearWarnings() throws java.sql.SQLException
      { }
   public String getCursorName() throws java.sql.SQLException
      { throw new java.sql.SQLException("Positioned Update not supported.", "S1C00"); }
   public java.sql.ResultSetMetaData getMetaData() throws java.sql.SQLException
      { return null; }

   public void updateArray(int columnIndex,java.sql.Array x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateArray(String columnName,java.sql.Array x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateAsciiStream(int columnIndex,java.io.InputStream x,int length) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateAsciiStream(String columnName,java.io.InputStream x, int length) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateBigDecimal(int columnIndex,java.math.BigDecimal x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateBigDecimal(String columnName,java.math.BigDecimal x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateBinaryStream(int columnIndex,java.io.InputStream x,int length) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateBinaryStream(String columnName,java.io.InputStream x, int length) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateBlob(int columnIndex,java.sql.Blob x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateBlob(String columnName,java.sql.Blob x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateBoolean(int columnIndex,boolean x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateBoolean(String columnName,boolean x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateByte(int columnIndex,byte x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateByte(String columnName,byte x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateBytes(int columnIndex,byte[] x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateBytes(String columnName,byte[] x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateCharacterStream(int columnIndex,java.io.Reader x,int length) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateCharacterStream(String columnName,java.io.Reader reader, int length) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateClob(int columnIndex,java.sql.Clob x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateClob(String columnName,java.sql.Clob x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateDate(int columnIndex,java.sql.Date x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateDate(String columnName,java.sql.Date x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateDouble(int columnIndex,double x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateDouble(String columnName,double x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateFloat(int columnIndex,float x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateFloat(String columnName,float x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateInt(int columnIndex,int x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateInt(String columnName,int x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateLong(int columnIndex,long x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateLong(String columnName,long x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateNull(int columnIndex) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateNull(String columnName) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateObject(int columnIndex,Object x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateObject(int columnIndex,Object x,int scale) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateObject(String columnName,Object x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateObject(String columnName,Object x,int scale) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateRef(int columnIndex,java.sql.Ref x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateRef(String columnName,java.sql.Ref x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateRow() throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateShort(int columnIndex,short x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateShort(String columnName,short x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateString(int columnIndex,String x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateString(String columnName,String x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateTime(int columnIndex,java.sql.Time x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateTime(String columnName,java.sql.Time x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateTimestamp(int columnIndex,java.sql.Timestamp x) throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void updateTimestamp(String columnName,java.sql.Timestamp x) throws java.sql.SQLException { throw new java.sql.SQLException(); }

   public void deleteRow() throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void moveToInsertRow() throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void moveToCurrentRow() throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void cancelRowUpdates() throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void insertRow() throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public void refreshRow() throws java.sql.SQLException { throw new java.sql.SQLException(); }
   public int getRow()  { 	return currentRow;  }
   public boolean first() { return false; }
   public boolean previous() {  return false;  }
   public boolean isFirst() { return false; }
   private boolean afterLast=false;
   public boolean last() 
   { 
	   try{
		   while(next());
	   }
	   catch(java.sql.SQLException sqle){}
	   afterLast=true;
	   return true;
   }
   public boolean isLast() { return false; }
   public void beforeFirst() throws java.sql.SQLException 
   { 
	   if(relation==null)
		   throw new java.sql.SQLException(); 
      if ((conditionIndex<0)&&(conditionValue!=null)) {
         iter=relation.index.keySet().iterator();
      } else {
         iter=relation.index.values().iterator();
      }
	  currentRow=0;
   }
   public boolean isBeforeFirst() { return (currentRow==0); }
   public void afterLast(){ last(); }
   public boolean isAfterLast(){return afterLast;}
   public boolean absolute(int i) { return true; }
   public boolean relative(int i) { return false; }
   public boolean rowDeleted() { return false; }
   public boolean rowInserted() { return false; }
   public boolean rowUpdated() { return false; }

   public int getConcurrency() { return 0; }
   public int getType() { return 0; }
   public void setFetchSize(int i) throws java.sql.SQLException { statement.setFetchSize(i); }
   public int getFetchSize() throws java.sql.SQLException { return statement.getFetchSize(); }
   public void setFetchDirection(int i) throws java.sql.SQLException { statement.setFetchDirection(i); }
   public int getFetchDirection() throws java.sql.SQLException { return statement.getFetchDirection(); }
   public int getResultSetConcurrency() throws java.sql.SQLException { return statement.getResultSetConcurrency(); }
   public int getResultSetType() throws java.sql.SQLException { return statement.getResultSetType(); }

    public int getHoldability() throws SQLException { return 0; }
    public Reader getNCharacterStream(int arg0) throws SQLException { return null; }
    public Reader getNCharacterStream(String arg0) throws SQLException { return null; }
    public NClob getNClob(int arg0) throws SQLException { return null; }
    public NClob getNClob(String arg0) throws SQLException { return null; }
    public String getNString(int arg0) throws SQLException { return null; }
    public String getNString(String arg0) throws SQLException { return null; }
    //public Object getObject(int arg0, Map arg1) throws SQLException { return getString(arg0); }
    public Object getObject(int arg0, Map<String, Class<?>> arg1) throws SQLException { return getString(arg0); }
    public Object getObject(String arg0, Map<String, Class<?>> arg1) throws SQLException { return getObject(findColumn(arg0),arg1); }
    //public Object getObject(String arg0, Map arg1) throws SQLException { return getObject(findColumn(arg0),arg1); }
    public RowId getRowId(int arg0) throws SQLException { return null; }
    public RowId getRowId(String arg0) throws SQLException { return null; }
    public SQLXML getSQLXML(int arg0) throws SQLException { return null; }
    public SQLXML getSQLXML(String arg0) throws SQLException { return null;}
    public boolean isClosed() throws SQLException { return false; }
    public void updateAsciiStream(int arg0, InputStream arg1) throws SQLException {}
    public void updateAsciiStream(String arg0, InputStream arg1) throws SQLException {}
    public void updateAsciiStream(int arg0, InputStream arg1, long arg2) throws SQLException {}
    public void updateAsciiStream(String arg0, InputStream arg1, long arg2) throws SQLException {}
    public void updateBinaryStream(int arg0, InputStream arg1) throws SQLException {}
    public void updateBinaryStream(String arg0, InputStream arg1) throws SQLException {}
    public void updateBinaryStream(int arg0, InputStream arg1, long arg2) throws SQLException {}
    public void updateBinaryStream(String arg0, InputStream arg1, long arg2) throws SQLException {}
    public void updateBlob(int arg0, InputStream arg1) throws SQLException {}
    public void updateBlob(String arg0, InputStream arg1) throws SQLException {}
    public void updateBlob(int arg0, InputStream arg1, long arg2) throws SQLException {}
    public void updateBlob(String arg0, InputStream arg1, long arg2) throws SQLException {}
    public void updateCharacterStream(int arg0, Reader arg1) throws SQLException {}
    public void updateCharacterStream(String arg0, Reader arg1) throws SQLException {}
    public void updateCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {}
    public void updateCharacterStream(String arg0, Reader arg1, long arg2) throws SQLException {}
    public void updateClob(int arg0, Reader arg1) throws SQLException {}
    public void updateClob(String arg0, Reader arg1) throws SQLException {}
    public void updateClob(int arg0, Reader arg1, long arg2) throws SQLException {}
    public void updateClob(String arg0, Reader arg1, long arg2) throws SQLException {}
    public void updateNCharacterStream(int arg0, Reader arg1) throws SQLException {}
    public void updateNCharacterStream(String arg0, Reader arg1) throws SQLException {}
    public void updateNCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {}
    public void updateNCharacterStream(String arg0, Reader arg1, long arg2) throws SQLException {}
    public void updateNClob(int arg0, NClob arg1) throws SQLException {}
    public void updateNClob(String arg0, NClob arg1) throws SQLException {}
    public void updateNClob(int arg0, Reader arg1) throws SQLException {}
    public void updateNClob(String arg0, Reader arg1) throws SQLException {}
    public void updateNClob(int arg0, Reader arg1, long arg2) throws SQLException {}
    public void updateNClob(String arg0, Reader arg1, long arg2)throws SQLException {}
    public void updateNString(int arg0, String arg1) throws SQLException {}
    public void updateNString(String arg0, String arg1) throws SQLException {}
    public void updateRowId(int arg0, RowId arg1) throws SQLException {}
    public void updateRowId(String arg0, RowId arg1) throws SQLException {}
    public void updateSQLXML(int arg0, SQLXML arg1) throws SQLException {}
    public void updateSQLXML(String arg0, SQLXML arg1) throws SQLException {}
    public boolean isWrapperFor(Class<?> iface) throws SQLException {return false;}
    public <T> T unwrap(Class<T> iface) throws SQLException {return null;}
}
