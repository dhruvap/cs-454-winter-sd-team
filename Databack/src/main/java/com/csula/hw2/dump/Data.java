package com.csula.hw2.dump;

import com.google.gson.Gson;
import com.mongodb.*;

import java.util.*;

public class Data {
    MongoClient con = null;
    private String hostname = "localhost";
    private int portnum = 27017;
    private String dbname = "crawler";
    DB db = null;
    DBCollection doc;
    DBCollection link;
    DBCollection meta;

    public Data() {
        connect();
    }

    public void connect()  {
        if (con == null) {
            try {
                System.out.println("..........CONNECTING TO MONGODB......");
                String host = this.hostname;
                int port = this.portnum;
                con = new MongoClient(host, port); // creating the connection
                db = con.getDB(this.dbname);           // getting the database
                link = db.getCollection("link");
                doc = db.getCollection("doc");
                meta = db.getCollection("meta");
                System.out.println("..........CONNECTED TO MONGODB......");
            }catch (Exception ex){
                System.out.println("..........ERROR CONNECTING TO MONGODB......");
                ex.printStackTrace();
            }
        }

    }


    public List<Map> getData(){
       // new Gson().
        DBCursor cursor = doc.find(new BasicDBObject());
        List<Map> list= new ArrayList<Map>();
        while(cursor.hasNext()){
            DBObject o = cursor.next();
            Set<String> set = o.keySet();
            Map m = new HashMap();
            for(String k : set){
                if( k.equals("doc")){
                    Map docM = new Gson().fromJson(o.get(k).toString(), Map.class);
                    m.put("fileLocation", docM.get("fileLocation"));

                    if(docM.get("id") != null){
                        String id = docM.get("id").toString();
                        DBCursor c = meta.find(new BasicDBObject("meta.id", id));
                        Map inMap = new HashMap();
                        while(c.hasNext()){
                            DBObject dbo = c.next();
                            Set<String> setIn = dbo.keySet();
                            for(String e : setIn){
                                inMap.put(e, dbo.get(e));
                            }
                            m.put("meta", inMap);
                        }
                    }
                }


            }

            list.add(m);

        }
        return list;
    }






    public static Data getObject(){
        return new Data();
    }

}
