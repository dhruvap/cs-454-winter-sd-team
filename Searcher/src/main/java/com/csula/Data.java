package com.csula;

import com.mongodb.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


public class Data {
    MongoClient con = null;
    private String hostname = "localhost";
    private int portnum = 27017;
    private String dbname = "crawler-1";
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


    public static class SearchResult{
       public String docId;
        public String term ;
        public String operator;
        public Double score;
        public Double scoreWt;
        public Double tfidf;
        public Double tfidfWt;
        public Double combWt;
        public Double comb;
        public String link;
        public List<String> otherTags = new ArrayList<String>();


        @Override
        public String toString() {
            return "{" +
                    "docId:\"" + docId + "\"" +
                    ", term:'" + term + '\'' +
                    ", operator:'" + operator + '\'' +
                    ", score:" + score +
                    ", link:'" + link + '\'' +
                    ", otherTags:"+otherTags+
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SearchResult that = (SearchResult) o;

            if (docId != null && that.docId != null ){
                if(this.link != null && that.link != null){
                    return docId.equals(that.docId) && (this.link.equals(that.link));
                }
                return docId.equals(that.docId);
            }

            return false;
        }

        @Override
        public int hashCode() {
            return docId != null ? docId.hashCode() : 0;
        }
    }

    public List<SearchResult> search(String word){
        BasicDBObject searchQuery = new BasicDBObject("term", word);
        DBCursor cursor = terms.find(searchQuery);
        List<SearchResult> results = new ArrayList<SearchResult>();
        while(cursor.hasNext()){

            DBObject o = cursor.next();
            BasicDBList list = (BasicDBList) o.get("doc");
            Double score = Double.parseDouble(o.get("score").toString());

            for(Object m : (list)){
                Map e = (Map)m;
                SearchResult res = new SearchResult();
                String docId = e.get("docid").toString();
                res.docId = docId;
                res.score = score;
                res.tfidf = Double.parseDouble(e.get("tf").toString()) * score;
                res.term = word;

                DBCursor innerCr = terms.find(new BasicDBObject("doc.docid", docId)).limit(20);
                while(innerCr.hasNext()){
                    DBObject i = innerCr.next();
                    res.otherTags.add(i.get("term").toString());
                }
                results.add(res);
            }

        }
        return results;

    }

    public List<String> searchSuggest(String word){
        BasicDBObject searchQuery = new BasicDBObject("term", "/.*"+word+".*/");
       // BasicDBObject searchQuery = new BasicDBObject();
        //searchQuery.put("term",
          //      new BasicDBObject("$regex","/"+word+"/")
       //                 .append("$options", "i"));

        DBObject A = QueryBuilder.start("term").is(Pattern.compile(word,
                Pattern.CASE_INSENSITIVE)).get();

        DBCursor cursor = terms.find(A);
        List<String> results = new ArrayList<String>();
        while(cursor.hasNext()){
            DBObject o = cursor.next();
            String term =  o.get("term").toString();
            results.add(term);

        }

        return  results;

    }




    public String getLink(String docId){
        DBObject o  =link.findOne(new BasicDBObject("id", docId));
        return o.get("url").toString();
    }

    public double getScore(String docId ,String link){
        DBCursor c  =termsInv.find(new BasicDBObject("doc.docid", docId));
        while(c.hasNext()){
            DBObject o = c.next();
            if(o.get("link").toString().toLowerCase().contains(link.substring(link.lastIndexOf("\\")).toLowerCase())){
                return Double.parseDouble(o.get("score").toString());
            }
        }
        return 0;
    }



}
