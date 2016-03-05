package com.csula.hw1.crawler;

import com.csula.hw3.indexer.Indexer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;


public class ResourceCrawler implements Callable<CrawlResult> {

    public ResourceCrawler(InputBean inputBean, String url, int depth,String parentDocId) {
        this.inputBean = inputBean;
        this.url = url;
        this.depth = depth;
        this.parentDocId = parentDocId;
    }

    private InputBean inputBean;
    private String url;
    private int depth;
    private String parentDocId;

    private String path  ="D:\\cs 454-john tran\\Crawler\\data";

    @Override
    public CrawlResult call() throws Exception {
        CrawlResult res = null;
        try{
            Data data = Data.getObject();
            URL uri = null;
            try{
                uri = new URL(url);
                if(depth > inputBean.getDepth()){
                    System.out.println("CANNOT CRAWL URL : ["+ url +"] AS IT MORE THAN DEPTH : ["+depth+"]..");
                    return res;
                }
            }catch (MalformedURLException ex){
                return res;
            }catch (Exception ex){
                ex.printStackTrace();
                return res;
            }

            boolean needToVisit = inputBean.isInsideDomain() && uri.getHost().contains(inputBean.getUrlObj().getHost());
            boolean alreadyVisited = data.isVisited(url) ;

            if(needToVisit && !alreadyVisited){
                String inputLine;
                String webpage = "";
                System.out.println("URL : " + uri.toString());
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(uri.openStream()));
                while ((inputLine = in.readLine()) != null){
                    webpage += inputLine;
                }
                in.close();

                res= new CrawlResult();
                String id =UUID.randomUUID().toString();
                res.setId(id);
                res.setDepth(depth);
                res.setParentId(parentDocId);
                res.setRaw(webpage);
                res.setResourceUrl(url);
                String ext = ".html";
                int ind = url.lastIndexOf(".");
                if(ind > 0){
                    ext = url.substring(url.lastIndexOf("."));
                    if(
                            ext.startsWith(".com") ||
                            ext.startsWith(".edu") ||
                            ext.startsWith(".org") ||
                            ext.startsWith(".net") ||
                            ext.startsWith(".php") ||
                            ext.startsWith(".co.in") ||
                            ext.startsWith(".in") ||
                            ext.startsWith(".us") ||
                            ext.startsWith(".co.us") ||
                            ext.startsWith(".asp") ||
                            ext.startsWith(".jsp") ||
                            ext.startsWith(".action") ||
                            ext.startsWith(".biz") ||
                            ext.startsWith(".me") ||
                            ext.startsWith(".uk") ||
                            ext.startsWith(".site") ||
                            ext.startsWith(".info")
                            ){
                        ext =".html";
                    }
                }

                String filePath = path + "\\"+id+(ext);

                FileWriter f = new FileWriter(filePath);
                f.write(webpage);
                f.close();

                res.setFileLocation(filePath);
                res.setParsedResource(findLinks(filePath));

                data.markVisited(url, res.getId());
                data.storeToDb(res);
                if(inputBean.isExtract()){
                    new Extractor().extract(res);
                }

                if(inputBean.isTermIndex()){
                    try {
                        Set<Map.Entry<String, Integer>> indexRes = Indexer.crawlAndIndexLink(new File(filePath)).entrySet();
                        for (Map.Entry<String, Integer> e : indexRes) {
                            data.addToWordIndx(e.getKey(), e.getValue(), res.getId());
                        }
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }

                    try {
                        Set<Map.Entry<String, Integer>> indexRes = Indexer.crawlAndIndexPageRank(res, inputBean).entrySet();
                        for (Map.Entry<String, Integer> e : indexRes) {
                            data.addToPageRankIndx(e.getKey(), e.getValue(), res.getId());
                        }
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }

                }

            }else  {
                System.out.println("CANNOT CRAWL URL : ["+ url +"] AS IT IS NOT IN SAME DOMAIN..");
            }


        }catch (Exception ex){
            ex.printStackTrace();
        }
        return res;
    }


    private Set<String> findLinks(String filePath){
        try {


            Document doc = Jsoup.parse(new File(filePath), "UTF-8");


            Elements links = doc.select("a[href]");
            Elements images = doc.select("img[src$=.png]");
            images.addAll(doc.select("img[src$=.jpg]"));
            images.addAll(doc.select("img[src$=.bmp]"));
            images.addAll(doc.select("img[src$=.jpeg]"));
            images.addAll(doc.select("img[src$=.ico]"));

            Elements styles = doc.select("stylesheet[rel]");
            Elements scripts =  doc.select("script[href]");

// img with src ending .png

            Set<String> sets = new HashSet<String>();
            for(Element e : links){
                String s = e.attr("href");
                if(s != null){
                    sets.add(s);
                }
            }

            for(Element e : images){
                String s =  e.attr("src");
                if(s != null){
                    sets.add(s);
                }
            }
            for(Element e : styles){
                String s =  e.attr("rel");
                if(s != null){
                    sets.add(s);
                }
            }
            for(Element e : scripts){
                String s =  e.attr("href");
                if(s != null){
                    sets.add(s);
                }
            }
            return sets;
                   }catch (Exception ex){
            ex.printStackTrace();
        }
        return new HashSet<String>(0);
    }

}
