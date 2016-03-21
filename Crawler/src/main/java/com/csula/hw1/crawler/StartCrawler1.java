package com.csula.hw1.crawler;

import com.csula.hw3.indexer.Indexer;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class StartCrawler {

    InputBean bean ;


    private InputBean parseInput( String[] args) throws URISyntaxException {
        InputBean inputBean = null;
        try {
            if ( (args != null && args.length >= 4)) {
                int depth = Integer.parseInt(args[1]);
                //int depth = 2;
                String urlStr = args[3];
                //String urlStr = "http://www.orlounge.com/index.html";
                //URL url = new URL(urlStr);
                String host = urlStr;
                boolean ex = false;
                if(args.length == 5 && args[4].equalsIgnoreCase("-e")){
                    ex= true;

                }
                boolean index = false;
                if(args.length == 6 && args[5].equalsIgnoreCase("-i")){
                    index = true;

                }

                inputBean = new InputBean();
                inputBean.setUrl(urlStr);
                inputBean.setUrlObj(new File(urlStr));
                inputBean.setInsideDomain(true);
                inputBean.setDepth(depth);
                inputBean.setExtract(ex);
                inputBean.setTermIndex(index);

            } else {
                System.out.println("Invalid Input");
            }
        }catch (NumberFormatException e){
            e.printStackTrace();
            System.out.println("Invalid Depth Value");
        } catch (Exception ex){
            ex.printStackTrace();
        }
        bean = inputBean;
        return inputBean;


    }

    ExecutorService service = Executors.newFixedThreadPool(60);

    private static void listFiles(File f, Set<String> fileSet){
        if(f.isDirectory()){
            File[]  files = f.listFiles();
            for(File e  : files ) {
                if(!e.isDirectory()){
                    fileSet.add(e.getAbsolutePath());
                }else {
                    listFiles(e, fileSet);
                }
            }

        }
    }

    public static void main(String[] args) throws URISyntaxException, ExecutionException, InterruptedException {
        StartCrawler me = new StartCrawler();
        me.parseInput(args);
        Data data = new Data();
        data.deleteAll();
        data.initAll();
        String folder = "D:\\cs 454-john tran\\en\\articles";
        File f = new File(folder);
        Set<String> files = new HashSet<String>();
        if(f.isDirectory()){
            listFiles(f, files);
        }
        int i = files.size();
        Set<String> temp = new HashSet<String>();
        int a = -1;
        //int t = 20;
        int t = files.size();
        for(String s : files){
            if(++a > t){
                break;
            }
            temp.add(s);
        }
        int x= -1;
        List<FutureTask> tasks = new ArrayList<FutureTask>();
        for(String s : temp){
            System.out.println(" ("+(++x)+" / "+i+")");
            ResourceCrawler rc = new ResourceCrawler(me.bean, s, 0, null);
            final FutureTask<CrawlResult> task = new FutureTask<CrawlResult>(rc);
            me.service.submit(task);
            tasks.add(task);

             task.get();
           // CrawlResult cr = task.get();
            //me.callCr(cr);

        }
        for(FutureTask e : tasks){
            e.get();
        }

        System.out.println("..............COMPLETED....");

       if(me.bean.isTermIndex()){
           Indexer.doTermScoring();
           Indexer.doPRScoring();
       }

        me.service.shutdown();
    }


    private void callCr(CrawlResult cr) throws ExecutionException, InterruptedException {
        if(cr == null){
            return;
        }
        int dep = cr.getDepth()+1;
        if(cr.getParsedResource() == null){
            return;
        }
        // System.out.println(" FOUND URLS : " + cr.getParsedResource() );
        for(String url : cr.getParsedResource()){

            if(bean.getDepth() <= dep+1){
                continue;
            }
            ResourceCrawler rc = new ResourceCrawler(bean, url, dep, cr.getId());
            FutureTask<CrawlResult> task = new FutureTask<CrawlResult>(rc);
            service.submit(task);
            //callCr(task.get());

        }
    }


}
