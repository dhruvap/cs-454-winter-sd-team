package com.csula.hw1.crawler;

import com.csula.hw3.indexer.ScoreFactor;
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
    DBCollection terms;
    DBCollection termsInv;
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
                terms = db.getCollection("terms");
                termsInv = db.getCollection("termsInv");
                meta = db.getCollection("meta");
                System.out.println("..........CONNECTED TO MONGODB......");
            }catch (Exception ex){
                System.out.println("..........ERROR CONNECTING TO MONGODB......");
                ex.printStackTrace();
            }
        }

    }

    public void addScoreWordIndx(String word, double score){



        BasicDBObject updateQuery = new BasicDBObject("term", word);
        BasicDBObject setNewFieldQuery = new BasicDBObject().append("$set", new BasicDBObject().append("score", score));

        terms.update(updateQuery, setNewFieldQuery);
    }
    public void addScorePRIndx(String word, double score){



        BasicDBObject updateQuery = new BasicDBObject("link", word);
        BasicDBObject setNewFieldQuery = new BasicDBObject().append("$set", new BasicDBObject().append("score", score));

        termsInv.update(updateQuery, setNewFieldQuery);
    }



    public void addToWordIndx(String word, int cnt, String doc){

        BasicDBObject o = new BasicDBObject();
        o.put("term", word);
        DBObject res = terms.findOne(o);
        if(res == null){
            BasicDBObject db = new BasicDBObject("term", word);
            db.put("doc", new ArrayList());
            terms.insert(db);
        }

        BasicDBObject docToInsert = new BasicDBObject("docid", doc);
        docToInsert.put("cnt", cnt);
        BasicDBObject updateQuery = new BasicDBObject("term", word);
        BasicDBObject updateCommand = new BasicDBObject("$push", new BasicDBObject("doc", docToInsert));
        terms.update(updateQuery, updateCommand);




    }

    public void addToPageRankIndx(String word, int cnt, String doc){

        BasicDBObject o = new BasicDBObject();
        o.put("link", word);
        DBObject res = termsInv.findOne(o);
        if(res == null){
            BasicDBObject db = new BasicDBObject("link", word);
            db.put("doc", new ArrayList());
            termsInv.insert(db);
        }

        BasicDBObject docToInsert = new BasicDBObject("docid", doc);
        docToInsert.put("cnt", cnt);
        BasicDBObject updateQuery = new BasicDBObject("link", word);
        BasicDBObject updateCommand = new BasicDBObject("$push", new BasicDBObject("doc", docToInsert));
        termsInv.update(updateQuery, updateCommand);




    }


    public long getTotalDocs(){
        return doc.count();
    }

    public static void main(String[] args) {
        new Data().getAllTermsForScoring();
    }

    public List<ScoreFactor> getAllTermsForScoring(){
        List<ScoreFactor> res =new ArrayList<ScoreFactor>();
        DBCursor cursor = terms.find();
        while(cursor.hasNext()){

            DBObject o = cursor.next();
            String term = (String) o.get("term");
            BasicDBList docs = (BasicDBList) o.get("doc");
            int cnt = docs.size();
            res.add(new ScoreFactor(term, cnt));

        }
        return res;
    }

    public List<ScoreFactor> getAllPRLinksForScoring(){
        List<ScoreFactor> res =new ArrayList<ScoreFactor>();
        DBCursor cursor = termsInv.find();
        while(cursor.hasNext()){

            DBObject o = cursor.next();
            String term = (String) o.get("link");
            BasicDBList docs = (BasicDBList) o.get("doc");
            int cnt = docs.size();
            res.add(new ScoreFactor(term, cnt));

        }
        return res;
    }



    public CrawlResult storeToDb(CrawlResult res){
       // new Gson().
        Map m = new HashMap();
        m.put("id", res.getId());
        m.put("fileLocation", res.getFileLocation());
        m.put("depth", res.getDepth());
        m.put("parentId", res.getParentId());
        m.put("resources", res.getParsedResource());
        m.put("raw", res.getRaw());
        m.put("url", res.getResourceUrl());
        BasicDBObject o = new BasicDBObject("doc", m);
        doc.insert(o);
        return res;
    }


    public CrawlResult storeToMeta(CrawlResult res, Map metaMap){
       // new Gson().

        BasicDBObject o = new BasicDBObject("meta", metaMap);
        meta.insert(o);
        return res;
    }

    public void markVisited(String url, String id){
        // new Gson().
        Map<String,String> map = new HashMap<String, String>();
        map.put("url", url);
        map.put("id", id);
        BasicDBObject o = new BasicDBObject(map);
        link.insert(o);
    }

    public boolean isVisited(String url){
        // new Gson().
        DBObject o = new BasicDBObject();
        o.put("url", url);
        DBCursor cursor = link.find(o);
        return cursor.hasNext();
    }


    public void deleteAll(){
        // new Gson().
        doc.drop();
        link.drop();
        meta.drop();
        terms.drop();
        termsInv.drop();

    }

    public void initAll(){
        // new Gson().

        db.createCollection("link", new BasicDBObject());
        db.createCollection("doc", new BasicDBObject());
        db.createCollection("meta", new BasicDBObject());
        db.createCollection("terms", new BasicDBObject());
        db.createCollection("termsInv", new BasicDBObject());

    }



    public static Data getObject(){
        return new Data();
    }

}
