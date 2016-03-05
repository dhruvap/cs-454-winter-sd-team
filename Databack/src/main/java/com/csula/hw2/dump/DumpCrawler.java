package com.csula.hw2.dump;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class DumpCrawler {


    public static void main(String[] args) throws URISyntaxException, ExecutionException, InterruptedException {
        DumpCrawler me = new DumpCrawler();
        List<Map> data = new Data().getData();
        for( Map m : data){
            System.out.println("--------------------");
            System.out.println(m);
            System.out.println("---------------------");
        }
    }




}
