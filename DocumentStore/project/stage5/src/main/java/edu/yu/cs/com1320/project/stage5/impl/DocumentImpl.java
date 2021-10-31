package edu.yu.cs.com1320.project.stage5.impl;

import java.net.URI;
import java.util.*;
import edu.yu.cs.com1320.project.stage5.Document;

public class DocumentImpl implements Document {
    protected URI uri;
    protected String text;
    protected byte[] binaryData;
    protected Map<String,Integer> count;
    private long time;
    
    
    public DocumentImpl(URI uri, String text){
        if(uri == null || text.length() == 0){
            throw new IllegalArgumentException();
        }
        this.uri = uri;
        this.text = text;
        String textclean = this.text.replaceAll("[^A-Za-z0-9 ]","");
        String correctext = textclean.toLowerCase();
        this.count = new HashMap<>();
        for (String s : correctext.split(" ")) {
            if(this.getWords().contains(s)){
                if(this.count.get(s) == null){
                    this.count.put(s,1);
                }
                
            else{
                this.count.put(s,this.count.get(s)+1);
            }
            }
        }
    }
        public DocumentImpl(URI uri, byte[] binaryData){
            if(uri == null || binaryData == null){
                throw new IllegalArgumentException();
            }
            this.uri = uri;
            this.binaryData = binaryData;
        }
    public int compareTo(Document o) {
        if(o == null){
            return 1;
        }
        else if (this.getLastUseTime() < o.getLastUseTime()){
            return -1;
        } 
        else if(this.getLastUseTime() > o.getLastUseTime()){
            return 1;
        }
        
        return 0;
       
    }

    @Override
    public String getDocumentTxt() {
        return text;
    }

    @Override
    public byte[] getDocumentBinaryData() {
        // 
        return this.binaryData;
    }

    @Override
    public URI getKey() {
        
        return this.uri;
    }
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(binaryData);
        return result;
        }   
    @Override
    public boolean equals(Object d){
        if(d == null){
            return false;
        }
        return this.hashCode() == ((Document)(d)).hashCode();
    }    
    
    @Override
    public int wordCount(String word) {
        String textclean = word.replaceAll("[^A-Za-z0-9 ]","");
        String correctext = textclean.toLowerCase();
        System.out.println(correctext + "  " + this.getWords().contains(correctext));
        if(this.binaryData != null || !this.getWords().contains(correctext)){
            return 0;
        }
        else{
            return this.count.get(correctext);
        }
    }
        
    

    @Override
    public Set<String> getWords() {
        Set<String> words = new HashSet<>();
        if(this.binaryData != null){
            return words;
        }
        String textclean = this.text.replaceAll("[^A-Za-z0-9 ]","");
        String correctext = textclean.toLowerCase();
        for(String s: correctext.split(" ")){
            words.add(s);
        }
        words.remove("");
        return words;
        
    }

    @Override
    public long getLastUseTime() {
        return this.time;
    }

    @Override
    public void setLastUseTime(long timeInNanoseconds) {
        this.time = timeInNanoseconds; 
    }
    @Override
    public Map<String, Integer> getWordMap() {
        HashMap<String, Integer> copy = new HashMap<String, Integer>();
        if(this.text != null){
            copy = new HashMap<String,Integer>(this.count);
            return copy;
        }
        else{
            return copy;
        }
    }
    @Override
    public void setWordMap(Map<String, Integer> wordMap) {
        if(this.text != null){
            this.count = (HashMap<String,Integer>)wordMap;
        }
        else{
            this.count = new HashMap<String,Integer>();
        }
    }
    
    
}
