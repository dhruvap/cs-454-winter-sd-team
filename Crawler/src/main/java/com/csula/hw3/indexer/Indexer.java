package com.csula.hw3.indexer;


import com.csula.hw1.crawler.CrawlResult;
import com.csula.hw1.crawler.Data;
import com.csula.hw1.crawler.InputBean;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class Indexer {
    public static int numDocuments;
    public final static HashSet<String> stopWords = new HashSet<String>();;
    static {
    /*    try{
            String is = Indexer.class.getClassLoader().getResource("stopwords.txt").toString();
            String s= "";
            BufferedReader br = new BufferedReader(new FileReader(is));
            while((s = br.readLine()) != null){
                stopWords.add(s.toLowerCase().trim());
            }
            br.close();
        }catch (Exception e){
            e.printStackTrace();
        }finally {

        }*/
    }

    public static void crawlAndIndexDir(File f){
        try{
            if(f != null && f.exists()){
                if(f.isDirectory()){
                    File[] files =  f.listFiles();
                    if(files != null){
                        for(File e : files){
                            crawlAndIndexDir(e);
                        }
                    }
                } else {
                    indexProcess(f, false);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static Map<String,Integer>   crawlAndIndexLink(File f){
        return indexProcess(f, true);
    }
     public static Map<String,Integer>   crawlAndIndexPageRank(CrawlResult result, InputBean inputBean){
         Map<String,Integer> wordFreq = new HashMap<String, Integer>();
         for(String s : result.getParsedResource()){
             boolean needToVisit = inputBean.isInsideDomain() && s.contains(inputBean.getUrlObj().getHost());
             if(needToVisit){
                 if(wordFreq.get(s.toLowerCase().trim()) != null){
                     int  cnt = wordFreq.get(s.toLowerCase().trim());
                     wordFreq.put(s.toLowerCase().trim(), cnt+1);
                 } else {
                     wordFreq.put(s.toLowerCase().trim(), 1);
                 }
             }
         }
        return wordFreq;
    }


    private Set<String> findLinks(File file){
        try {

            Document doc = Jsoup.parse(file, "UTF-8");
            Elements links = doc.select("a[href]");
            Set<String> sets = new HashSet<String>();
            for(Element e : links){
                String s = e.attr("href");
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

    public static void doTermScoring(){
        try{
            Data  data = Data.getObject();
        long totalDoc = data.getTotalDocs();
        List<ScoreFactor> terms = data.getAllTermsForScoring();
            for(ScoreFactor e : terms){
                double idf = calculateIdf(totalDoc, e.getCount());
                data.addScoreWordIndx(e.getTerm(), idf);
            }

        }catch (Exception ex){

        }
    }
     public static void doPRScoring(){
        try{
            Data  data = Data.getObject();
            long totalDoc = data.getTotalDocs();
            List<ScoreFactor> terms = data.getAllPRLinksForScoring();
            for(ScoreFactor e : terms){
                double idf = calculateIdf(totalDoc, e.getCount());
                data.addScorePRIndx(e.getTerm(), idf);
            }

        }catch (Exception ex){

        }
    }

    public static List<String> extractText(Reader reader) throws IOException {
        final ArrayList<String> list = new ArrayList<String>();

        ParserDelegator parserDelegator = new ParserDelegator();
        HTMLEditorKit.ParserCallback parserCallback = new HTMLEditorKit.ParserCallback() {
            public void handleText(final char[] data, final int pos) {
                list.add(new String(data));
            }
            public void handleStartTag(HTML.Tag tag, MutableAttributeSet attribute, int pos) { }
            public void handleEndTag(HTML.Tag t, final int pos) {  }
            public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, final int pos) { }
            public void handleComment(final char[] data, final int pos) { }
            public void handleError(final java.lang.String errMsg, final int pos) { }
        };
        parserDelegator.parse(reader, parserCallback, true);
        return list;
    }
    public static Map<String,Integer>  indexProcess(File f,boolean trimHtml ){
        Map<String,Integer> wordFreq = new HashMap<String, Integer>();

        try{
            if(f.exists()){


                    BufferedReader br = new BufferedReader(new FileReader(f));
                    String line = null;
                    while((line = br.readLine()) != null){
                        if(trimHtml){
                            line = Jsoup.parse(line.trim()).text();
                        }
                        String[] arr = line.split(" ");
                        for(String s : arr){
                            if(stopWords.contains(s.toLowerCase().trim())){
                                continue;
                            }

                            if(wordFreq.get(s.toLowerCase().trim()) != null){
                                int  cnt = wordFreq.get(s.toLowerCase().trim());
                                wordFreq.put(s.toLowerCase().trim(), cnt+1);
                            } else {
                                wordFreq.put(s.toLowerCase().trim(), 1);
                            }
                        }
                    }
                    br.close();



            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return wordFreq;


    }
/*

    public ArrayList loadDocs(String folderPath) throws IOException {
        //nb returns arraylist, each element is an array size 2



        BufferedReader br = new BufferedReader(new FileReader(new File(docPath)));

        String line;
        String doc;
        String user;
        String[] userAndDoc;
        int countLine = 0;
        int parseErrs = 0;

        while ((line = br.readLine()) != null) {
            //System.out.println(line);
            try {
                //each line contains the user's name, then their document, seperated by "**MARK**"
                userAndDoc = line.split("\\*\\*MARK\\*\\*");
                user = userAndDoc[0];
                doc = userAndDoc[1];
                //System.out.println(user+doc);
                if (doc.length() > 3) {
                    userDocs.add(userAndDoc);
                }

                countLine++;
            } catch (Exception e) {
                parseErrs++;
            }


        }
        System.out.println(parseErrs);

        System.out.println("Num lines: " + countLine);
        this.numDocuments = userDocs.size();
        System.out.println("num docs: " + this.numDocuments);

        return userDocs;
    }


    public HashMap loadVocabMap() throws IOException {
        //contains each unique word in the corpus, plus the number of documents it's found in.
        //format: [word frequency]
        //returned as a word:frequency map

        String vocabFilePath = "/path/to/docFreqs.data";

        HashMap<String, Integer> vocabCount = new HashMap();
        String line = "";
        BufferedReader br = new BufferedReader(new FileReader(new File(vocabFilePath)));
        String[] thisWordAndFreq;
        String key;
        Integer value;
        while ((line = br.readLine()) != null) {
            thisWordAndFreq = line.split(" ");
            key = thisWordAndFreq[0];
            value = Integer.parseInt(thisWordAndFreq[1]);
            if (thisWordAndFreq[0].length() > 2) { //ie if a word is actually there and not whitespace etc.
                vocabCount.put(key, value);
            }
        }
        return vocabCount;

    }

    private void writeLine(String user, ArrayList<Double> tfidfLongMatrix) throws IOException {
        //writes tf-idf weighted vectors to file
        String matrixFilePath = "/destinationFolder/tfidfVectors.data";
        FileWriter fw = new FileWriter(matrixFilePath, true);
        fw.write(user + " ");
        DecimalFormat fourDForm = new DecimalFormat("#.#####");
        Iterator iter = tfidfLongMatrix.iterator();
        while (iter.hasNext()) {
            fw.write(String.valueOf(fourDForm.format(iter.next())) + " ");
        }
        fw.write("\n");
        fw.close();
    }
*/


 /*   public static void main(String[] args) throws IOException {
        int count = 0;
        TFIDF mtl = new TFIDF();
        ArrayList vocabList = new ArrayList();

        HashMap vocabAndFreq = mtl.loadVocabMap();
        vocabList = mtl.makeVocabList(); //update vocabList defined in class
        System.out.println("vocab list size:  " + vocabList.size());
        ArrayList documents = mtl.loadUserDocs(); //rem that each elem is [[uname][doc]]
        ArrayList<Double> initDocMatrix;
        ArrayList docMatrices;
        ArrayList<Double> tfidfLongMatrix;
        String[] docSplit;
        String docStr;
        for (int i = 0; i < documents.size(); i++) {
            initDocMatrix = mtl.initialiseDocMatrix(vocabList);
            String[] thisDocList = (String[]) documents.get(i);
            String user = thisDocList[0];
            String userDoc = thisDocList[1];
            tfidfLongMatrix = makeTfidfMatrix(userDoc, vocabAndFreq, initDocMatrix, vocabList);
            mtl.writeLine(user, tfidfLongMatrix);
            if (i % 500 == 0) {
                System.out.println(i + " of " + documents.size() + " written");
            }
        }
    }
*/

    private ArrayList makeVocabList() throws IOException {
        // as well as vocab/frequency hashmap,
        // i need an arraylist, which is used to ensure the placing of tf-idf scores in the same order in the vector.

        String vocabFilePath = "C://datasets//twitter_data//sep11//forCossim//docFreqs_790-839.data";
        ArrayList vocab = new ArrayList();
        String line = "";
        BufferedReader br = new BufferedReader(new FileReader(new File(vocabFilePath)));
        String[] thisWordAndFreq;
        String word;


        while ((line = br.readLine()) != null) {
            thisWordAndFreq = line.split(" ");
            word = thisWordAndFreq[0];
            if (thisWordAndFreq[0].length() > 2) { //ie if a word is actually there and not whitespace etc.
                vocab.add(word);
            }
        }
        return vocab;

    }


    private static ArrayList<Double> makeTfidfMatrix(String userDoc, HashMap vocabAndFreq, ArrayList<Double> docMatrix, ArrayList vocabList) {
        String[] docSplit = userDoc.split(" ");
        //find unique set of words
        Set<String> wordSet = new HashSet(Arrays.asList(docSplit));

        Iterator setIter = wordSet.iterator();
        int docLen = docSplit.length;
        int errs = 0;

        while (setIter.hasNext()) {
            String word = (String) setIter.next();
            try {
                Double wordTfidfScore = getWordTfidf(word, docSplit, vocabAndFreq, docLen);
                //find place of that word in vocab
                int place = vocabList.indexOf(word);
                docMatrix.set(place, wordTfidfScore);

            } catch (Exception e) {
                errs++;//ie word isn't in vocab. ie was a stop word etc.
            }

        }
        //System.out.println(errs);
        return docMatrix;
    }


    private static Double getWordTfidf(String word, String[] docSplit, HashMap vocabAndFreq, int docLen) {
        double tf = getTf(word, docSplit, docLen);
        double idf = getIdf(word, (Integer) vocabAndFreq.get(word));
        double tfidf = tf * idf;
        return tfidf;
    }


    private static double getIdf(String word, int numDocsContainingWord) {
        return Math.log(((numDocuments * 1.0) / numDocsContainingWord));
    }

    private static double calculateIdf(long totalDocs, int numDocsContainingWord) {
        return Math.log(((totalDocs * 1.0) / numDocsContainingWord));
    }


    private static double getTf(String word, String[] docSplit, int docLen) {
        //number of occurences of this word in document
        int termFreq = 0;
        for (int k = 0; k < docSplit.length; k++) {
            if (word == docSplit[k]) {
                termFreq++;
            }
        }
        return (termFreq / (float) docSplit.length);
    }


    private ArrayList initialiseDocMatrix(ArrayList vocabList) {
        //set up an initial vector of the correct size (the size of the corpus vocab.) comprised of zeros
        ArrayList initDocMatrix = new ArrayList();
        for (int i = 0; i < vocabList.size(); i++) {
            initDocMatrix.add(0.0);

        }
        return initDocMatrix;
    }

}
