package edu.yu.cs.com1320.project.stage3.impl;
import java.net.URI;
import java.util.*;
import edu.yu.cs.com1320.project.stage3.Document;

public class DocumentImpl implements Document{
    protected URI uri;
    protected String text;
    protected byte[] binaryData;
    protected Map<String,Integer> count;
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
    public String getDocumentTxt(){
        return text;
    }
    public byte[] getDocumentBinaryData(){
        return this.binaryData;
    }
    public URI getKey(){
        return this.uri;
    }
    @Override
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
    
    public Set<String> getWords(){
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
    public int wordCount(String word){
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
}