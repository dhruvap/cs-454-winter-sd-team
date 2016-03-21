package com.csula.hw1.crawler;

import com.csula.hw3.indexer.Indexer;
import com.uwyn.jhighlight.tools.StringUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
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

    private String path  ="D:\\Dhruva\\Crawler\\src\\main\\data";


    @Override
    public CrawlResult call() throws Exception {
        CrawlResult res = null;
        Data data = Data.getObject();
        try{
            File uri = null;
            try{
                uri = new File(url);
                if(depth >= inputBean.getDepth()){
                    System.out.println("CANNOT CRAWL URL : ["+ url +"] AS IT MORE THAN DEPTH : ["+depth+"]..");
                    return res;
                }
            }catch (Exception ex){
                ex.printStackTrace();
                return res;
            }

            boolean needToVisit = true;
            boolean alreadyVisited = data.isVisited(url) ;

            if(needToVisit && !alreadyVisited){
                String inputLine;
                String webpage = "";
                System.out.println("URL : " + uri.toString());
                FileReader fr = null;
                try{
                   fr =  new FileReader(uri);
                }catch (Exception ex){
                    return null;
                }
                BufferedReader in = new BufferedReader(
                       fr );
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

                String filePath = url;

              /*  FileWriter f = new FileWriter(filePath);
                f.write(webpage);
                f.close();
*/
                res.setFileLocation(filePath);

                Set<String> links = findLinks(filePath);
                Set<String> toSave = new HashSet<String>();
                String ori = null;
                for(String s : links){
                    s = StringEscapeUtils.unescapeHtml(s) ;
                    if((s.startsWith("http:") || s.startsWith("https:")) || !(s.endsWith(".html") || s.endsWith(".htm"))  ){
                        continue;
                    }
                    ori = s;
                    String newStr = "";
                    int  j = 0;
                    while(s.startsWith("../")){
                        j++;
                        if(s.startsWith("../")){
                            int  i = url.lastIndexOf("\\");
                            if(i == -1){
                                i = url.lastIndexOf("/");
                            }
                             if(i == -1){
                                 newStr = url;
                             }else {
                                 newStr = url.substring(0,url.lastIndexOf("\\"));
                             }

                        }
                        s = s.replaceFirst("../","");
                    }
                    String addStr =url;
                    for(int x= 0; x<= j ;x++){
                        int v = addStr.lastIndexOf("\\");

                        addStr = addStr.substring(0, v != -1 ? v : addStr.length() -1);
                    }
                    toSave.add((addStr+"\\"+s).replaceAll("/","\\\\"));


                }
                res.setParsedResource(toSave);

                data.markVisited(url, res.getId());
                data.storeToDb(res);
                if(inputBean.isExtract()){
                    new Extractor().extract(res);
                }

                if(inputBean.isTermIndex()){
                    try {
                        Set<Map.Entry<String, Integer>> indexRes = Indexer.crawlAndIndexLink(new File(filePath)).entrySet();
                        int totalTerm = 0;
                        for(Map.Entry<String, Integer> e: indexRes){
                            totalTerm+= e.getValue();
                        }
                        for (Map.Entry<String, Integer> e : indexRes) {
                            double tf = Indexer.getTf(e.getValue(), totalTerm);

                            data.addToWordIndx(e.getKey(), e.getValue(), res.getId(), tf);
                        }
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }

                    try {
                        Set<Map.Entry<String, Integer>> indexRes = Indexer.crawlAndIndexPageRank(res, inputBean).entrySet();
                        for (Map.Entry<String, Integer> e : indexRes) {
                            data.addToPageRankIndx(e.getKey(), e.getValue(), res.getId(), res.getParsedResource().size());
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
        } finally{
           data.con.close();
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
