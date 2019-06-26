package de.intsys.krestel.SearchEngine;


import java.util.*;

public class BooleanRetrieval {

    static String searchBooleanQuery(String query, Map<String, Long> dictionary) {

        query = "(" + query + ")";
        query = query.replaceAll("(\\()", "\\( ");
        query = query.replaceAll("(\\))", " \\)");
        query = query.replaceAll(" +", " ");
        String[] queryArray = query.split(" ");

        Stack<String> operators = new Stack<String>();
        Stack<List<Integer>> operandPostingList = new Stack<>();
        //System.out.println(InvertedIndexer.articleIDList);
        try {

            for (String term : queryArray) {
                if (!term.matches("AND|OR|NOT|INOT|(\\()|(\\))")) operandPostingList.push(InvertedIndexer.getPostingList(Article.PorterStem(term.toLowerCase()), dictionary)); //a
                else if(term.matches("AND|OR|NOT|INOT") && operators.isEmpty()) operators.push(term);//b
                else if(term.matches("AND|OR|NOT|INOT")&& !operators.isEmpty() && (hasPrecedence(term)>hasPrecedence(operators.peek()))) operators.push(term);//c
                else if (term.equals("(")) operators.push(term);//d
                else if (term.equals(")")) {//e
                    String operator1 = operators.pop();
                    while (!operator1.equals("(")) {



                        if (!operandPostingList.empty()) {
                            List<Integer> operand1PostingList = operandPostingList.pop();

                            if (operator1.equals("AND"))
                                operand1PostingList = andQuery(operandPostingList.pop(), operand1PostingList);
                            else if (operator1.equals("OR"))
                                operand1PostingList = orQuery(operandPostingList.pop(), operand1PostingList);
                            else if (operator1.equals("NOT")){
                                //operand1PostingList = andQuery(operandPostingList.pop(), notQuery(operand1PostingList));
                                operand1PostingList = not1Query(operandPostingList.pop(), operand1PostingList);}
                            else if (operator1.equals("INOT")) operand1PostingList = notQuery(operand1PostingList);
                            operator1 = operators.pop();
                            operandPostingList.push(operand1PostingList);
                            System.out.println("Intermediate Result : " + operand1PostingList);
                        } else {
                            System.out.println("Incorrect syntax");
                            break;

                        }
                    }
                }
                else//f
                {

                    String operator1 = operators.pop();



                    List<Integer> operand1PostingList = operandPostingList.pop();
                    if (!operandPostingList.empty()) {

                        if (operator1.equals("AND"))
                            operand1PostingList = andQuery(operandPostingList.pop(), operand1PostingList);
                        else if (operator1.equals("OR"))
                            operand1PostingList = orQuery(operandPostingList.pop(), operand1PostingList);
                        else if (operator1.equals("NOT")) {
                            //operand1PostingList = andQuery(operandPostingList.pop(), notQuery(operand1PostingList));
                            operand1PostingList = not1Query(operandPostingList.pop(), operand1PostingList);
                        }
                        else if (operator1.equals("INOT")) operand1PostingList = notQuery(operand1PostingList);
                        //operator1 = operators.pop();
                        operandPostingList.push(operand1PostingList);
                        System.out.println("Intermediate Result : " + operand1PostingList);
                    } else {
                        System.out.println("Incorrect syntax");
                        break;

                    }
                    operators.push(term);



                }

            }
        } catch (EmptyStackException e) {
            // TODO Auto-generated catch block
            System.out.println("Incorrect Syntax");
            e.printStackTrace();

        }
        if (!operandPostingList.empty()) {
            return Arrays.toString(operandPostingList.pop().toArray());
        } else {
            System.out.println("Incorrect syntax");

        }
        return null;

    }
    static Integer hasPrecedence(String operatorTerm){
        int precedence=5;
        if(operatorTerm.equals("AND")) precedence=4;
        else if (operatorTerm.equals("OR")) precedence=3;
        else if (operatorTerm.equals("NOT")) precedence=4;
        else if (operatorTerm.equals("INOT")) precedence=13;
        else if (operatorTerm.equals(")")) precedence=1;
        else if (operatorTerm.equals("(")) precedence=1;


        return precedence;
    }

    static List<Integer> notQuery(List<Integer> postingList){
        List<Integer> result = new ArrayList(Constants.docIDs);
//        System.out.println(Constants.docIDs);
//
//        //result=Constants.docIDs;
//        System.out.println(result);
        int total_docs = Constants.docIDCount;
        // Implementing the not method
        for(int i=0;i<postingList.size();i++)
        {
            result.remove(postingList.get(i));
        }

        return result;
    }
    static List<Integer> not1Query(List<Integer> postingList1,List<Integer> postingList2){
        List<Integer> result = postingList1;
//        System.out.println(Constants.docIDs);
//
//        //result=Constants.docIDs;
//        System.out.println(result);
        int total_docs = Constants.docIDCount;
        // Implementing the not method
        for(int i=0;i<postingList2.size();i++)
        {
            if (result.contains(postingList2.get(i))){

                result.remove(postingList2.get(i));}
        }

        return result;
    }

    static List<Integer> andQuery(List<Integer> postingList1,List<Integer> postingList2) {

        List<Integer> result = new ArrayList<>();

        //Set indices to iterate two lists. I use i, j
        int i = 0;
        int j = 0;
        int postingSize1=postingList1.size();
        int postingSize2=postingList2.size();
        if(postingSize1>postingSize2)
        {
            List<Integer> temp=new ArrayList<>(postingList2);
            postingList2=postingList1;
            postingList1=temp;
        }

        while (i != postingList1.size() && j != postingList2.size()) {

            //Implement the intersection algorithm
            int x = postingList1.get(i);
            int y = postingList2.get(j);
            if (x == y) {
                result.add(x);
                i++;
                j++;
            } else if (x < y)
                i++;
            else
                j++;
        }
        return result;
    }
    static List<Integer> orQuery(List<Integer> postingList1,List<Integer> postingList2) {
        List<Integer> result = new ArrayList<>();
        int i = 0;
        int j = 0;
        while(i!=postingList1.size() && j!=postingList2.size()){
            int x = postingList1.get(i);
            int y = postingList2.get(j);
            if(x == y){
                result.add(x);
                i++;
                j++;
            }
            else if(x < y){
                result.add(x);
                i++;
            }
            else{
                result.add(y);
                j++;
            }
        }

        if (i == postingList1.size()){
            while (j != postingList2.size()){
                int u = postingList2.get(j);
                result.add(u);
                j++;
            }
        }
        else{
            while (i != postingList1.size()){
                int v = postingList1.get(i);
                result.add(v);
                i++;
            }
        }
        return result;
    }





}


