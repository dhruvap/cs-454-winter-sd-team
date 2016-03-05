package com.csula.hw1.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
public class Extractor {

    public void extract(CrawlResult cr ){
        try{
            Data data = new Data();
            Document doc = Jsoup.parse(new File(cr.getFileLocation()), "UTF-8");
            if(cr.getFileLocation().endsWith(".html") || cr.getFileLocation().endsWith(".htm")  ){
                Elements metas = doc.select("meta");
                Map m  = new HashMap();
                m.put("id", cr.getId());
                m.put("location", cr.getFileLocation());
                for(Element e : metas){
                    Attributes attrs = e.attributes();
                    for(Attribute a : attrs){
                        m.put(a.getKey().toString(), a.getValue().toString());
                    }

                }
                data.storeToMeta(cr, m);
            }


        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
