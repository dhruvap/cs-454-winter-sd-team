package com.csula;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;

@Controller
public class SearchServlet {
    final Data data = new Data();


    @RequestMapping(value = "Search.html" , method = RequestMethod.GET)
    public ModelAndView get(){
        ModelAndView model = new ModelAndView("result");
        model.addObject("top",  data.getTop(5));
        return model;
    }

    @RequestMapping(value = "/Main.html" , method = RequestMethod.GET)
    public ModelAndView getIndex(){
        ModelAndView model = new ModelAndView("main");
        model.addObject("top",  data.getTop(5));
        return model;
    }

    @RequestMapping(value = "google.html")
    private String demo(){
        return "google-demo";
    }


    @RequestMapping(value = "Suggest.html" , method = RequestMethod.GET)
    public @ResponseBody String suggest(@RequestParam("q") String query){
        if(query != null && !query.trim().isEmpty()){
            String[] toSuggest = query.trim().split(" ");

            List<String> res= data.searchSuggest(toSuggest[toSuggest.length-1]);
            List<SuggestResult> sr =new ArrayList<SuggestResult>();
            for(String s : res){
                sr.add(new SuggestResult(s));
            }
            return sr.toString();
        }

        return "";

    }

    public static class SuggestResult{
        private String term;

        @Override
        public String toString() {
            return "{" +
                    "\"term\":\"" + term + "\"" +
                    '}';
        }

        public SuggestResult(String term) {
            this.term = term;
        }

        public String getTerm() {
            return term;
        }

        public void setTerm(String term) {
            this.term = term;
        }
    }


    @RequestMapping(value = "Search.html" , method = RequestMethod.POST)
    public ModelAndView doPost(@RequestParam("q") String query, HttpServletRequest request, HttpServletResponse response){
        List<Data.SearchResult> finRes = new ArrayList<Data.SearchResult>();
        try{

            if(query != null && !query.trim().isEmpty()){
                List<String> terms = new ArrayList<String>();

                String operator = null;
                Set<Data.SearchResult> res = null;

                if((query.contains(" OR ") || query.contains(" or ")) ){
                    query =  query.replaceAll(" or ", " OR ");

                    String[] listOR = query.split(" OR ");
                    terms.addAll(Arrays.asList(listOR));
                    operator = "OR";

                    List<Data.SearchResult> old= null;
                    for(String term : terms){
                        if(old == null){
                            old = data.search(term.trim());
                        }else {
                            res = doOpProcess(operator, data.search(term.trim()), old);
                        }
                    }

                } else if((query.contains(" AND ") || query.contains(" and ")) ){
                    query = query.replaceAll(" and ", " AND ");
                    String[] listOR = query.split(" AND ");
                    terms.addAll(Arrays.asList(listOR));
                    operator = "AND";
                    List<Data.SearchResult> old= null;
                    for(String term : terms){
                        if(old == null){
                            old = data.search(term.trim());
                        }else {
                            res = doOpProcess(operator, data.search(term.trim()), old);
                        }
                    }
                }else {
                    List<Data.SearchResult> old= null;
                    res = doOpProcess(null, data.search(query.trim()), new ArrayList<Data.SearchResult>());
                }

                for(Data.SearchResult e : res){
                    e.link = data.getLink(e.docId);
                    double s  = data.getScore(e.docId, e.link );
                    if(s != 0){
                        MathContext mc = new MathContext(4);
                        e.score = s;
                        Double linkrank=  s;
                        BigDecimal bd1 = new BigDecimal(linkrank);
                        BigDecimal bd2 = new BigDecimal(0.3d);
                        BigDecimal result1 = bd1.multiply(bd2,mc);

                        BigDecimal bd3 = new BigDecimal(e.tfidf);
                        BigDecimal bd4 = new BigDecimal(0.7d);
                        BigDecimal result2 = bd3.multiply(bd4,mc);

                        BigDecimal sum=result1.add(result2, mc);



                        e.tfidfWt = result2.doubleValue();
                        e.scoreWt = result1.doubleValue();
                        e.comb = (e.tfidf * 0.7) + ( e.score * 0.3);
                        e.combWt = sum.doubleValue();
                        finRes.add(e);
                    }


                }

                        Collections.sort(finRes, new Comparator<Data.SearchResult>() {
                    @Override
                    public int compare(Data.SearchResult o1, Data.SearchResult o2) {
                      return  o1.score.equals(o2.score) ? 0 : (( o1.score > o2.score)? -1 : 1);
                    }
                } );







                finRes = new ArrayList<Data.SearchResult>(new LinkedHashSet<Data.SearchResult>(finRes));





            }

        }catch (Exception ex){

        }

        ModelAndView modelAndView = new ModelAndView("result");
        modelAndView.addObject("res", finRes);
        modelAndView.addObject("q", query);
        return modelAndView;
    }


    private Set<Data.SearchResult> doOpProcess(String op, List<Data.SearchResult> curr, List<Data.SearchResult> old){
        Set<Data.SearchResult> res = new LinkedHashSet<Data.SearchResult>();
        if("AND".equalsIgnoreCase(op)){
            return new HashSet<Data.SearchResult>(ListUtils.intersection(curr, old));
        }
        return new LinkedHashSet<Data.SearchResult>(ListUtils.union(curr, old));

    }


}
