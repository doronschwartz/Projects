package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.Trie;

import java.util.*; 
import java.util.List.*;

public class TrieImpl<Value> implements Trie<Value>{
    private Node root;
    
    private ArrayList<Value> prefixsort;
    class Node<Value>{
        
        protected List<Value> v = new ArrayList<>();
        protected Node[] links = new Node[36];
        }
    
    public TrieImpl(){

    }
    public void put(String key, Value val)
    {
        if(key == null){
            throw new IllegalArgumentException();
        }
        String clean = key.replaceAll("[^A-Za-z0-9 ]","");
        String correct = clean.toLowerCase();
        System.out.println(correct);
        //deleteAll the value from this key
        if (val == null)
        {
            return;
        }
        else
        {
            this.root = put(this.root, correct, val, 0);
        }
    }
    private Node put(Node x, String key, Value val, int d)
    {
        
        //create a new node
        if (x == null)
        {
            x = new Node();
        }
        //we've reached the last node in the key,
        //set the value for the key and return the node
        if (d == key.length())
        {
            if(!(x.v.contains(val))){

            x.v.add(val);}
            return x;
        }
        //proceed to the next node in the chain of nodes that
        //forms the desired key
        char c = key.charAt(d);
        if ((int)(c)<58){
            x.links[(int)(c)-48] = this.put(x.links[(int)(c)-48], key, val, d + 1);
        }
        else{
            x.links[(int)c - 87] = this.put(x.links[(int)c - 87], key, val, d + 1);
        }
        return x;
    }
    private Node get(String key)
        {
            String clean = key.replaceAll("[^A-Za-z0-9 ]","");
            clean.toLowerCase();
            Node x = get(root, clean, 0);
            if (x == null){ 
                return null;
            }
            return x;
}
    private Node get(Node x, String key, int d)
    { // Return value associated with key in the subtrie rooted at x.
        if (x == null){ 
            return null;
        }
        if (d == key.length()){
            return x;
        } 
        char c = key.charAt(d); // Use dth key char to identify subtrie.
        if ((int)(c)<58){
            return get(x.links[(int)(c)-48], key, d+1);
        }
        else{
            return get(x.links[(int)c - 87],key, d+1);
        }
        
    }
    public List<Value> getAllWithPrefixSorted(String prefix,Comparator<Value> comparator){
        if(prefix== null || comparator == null){
            throw new IllegalArgumentException();
        }
        this.prefixsort = new ArrayList<>();
        if(prefix.equals("")){
            return this.prefixsort;
        }
        String clean = prefix.replaceAll("[^A-Za-z0-9 ]","");
        String correct = clean.toLowerCase();
        this.getAllWithPrefix(correct);
        Collections.sort(this.prefixsort,comparator);
        return this.prefixsort;
}
    private void getAllWithPrefix(String prefix){
        Node header = this.get(prefix);
        if(header == null){
            return;
        }
        this.prefixsort.addAll(header.v);
        if(!(this.emptyChecker(header.links))){
        for(int i = 0; i < header.links.length;i++){
            if(header.links[i] !=null){
            this.prefixrecurse(header.links[i]);
        }
    }
    }
    }
    private void prefixrecurse(Node x){
        if (x.v != null){
            for(int i =0; i < x.v.size();i++){
                if(!(this.prefixsort.contains(x.v.get(i)))){
                    this.prefixsort.add((Value)x.v.get(i));
                }
            }
        
        }
        if(!(this.emptyChecker(x.links))){
        for(int i = 0;i < x.links.length;i++){
            if(x.links[i] !=null){
            prefixrecurse(x.links[i]);
        }
    }
    }
    }
    public List<Value> getAllSorted(String key,Comparator<Value> comparator){
        if(key == null || comparator == null){
            throw new IllegalArgumentException();
        }
        String clean = key.replaceAll("[^A-Za-z0-9 ]","");
        String correct = clean.toLowerCase();
        Node keymatch = this.get(correct);
        if(keymatch == null){
            return new ArrayList<Value>();
        }
        Collections.sort(keymatch.v,comparator); 
        return keymatch.v;
    }
    
    public Value delete(String key,Value val){
        if(key == null || val == null){
            throw new IllegalArgumentException();
        }
        String clean = key.replaceAll("[^A-Za-z0-9 ]","");
        String correct = clean.toLowerCase();
        Value returner = null;
        Node keymatch = this.get(correct);
        int count = 0;
        if(keymatch == null){
            return null;
        }
        for (Iterator<Value> iterator = keymatch.v.iterator();iterator.hasNext();){
            Value v = iterator.next();
            if (v.equals(val)) {
                iterator.remove();
                count++;
                returner = v;
            }
        }
        if(count > 0) {
            return returner;
        } 
        else{return null;}
    }
    public Set<Value> deleteAll(String key){
        if(key == null){
            throw new IllegalArgumentException();
        }
        Set<Value> deletedvalues = new HashSet<>();
        String clean = key.replaceAll("[^A-Za-z0-9 ]","");
        String correct = clean.toLowerCase();
        Node x = this.get(correct);
        if (x == null)
        {
            return null;
        }
        deletedvalues.addAll(x.v);
        x.v.clear();
        return deletedvalues;
    }
    

    public Set<Value> deleteAllWithPrefix(String prefix){
        if(prefix == null){
            throw new IllegalArgumentException();
        }
        this.prefixsort = new ArrayList<>();
        Set<Value> deletedvalues = new HashSet<>();
        if(prefix.equals("")){
            return deletedvalues;
        }
        String clean = prefix.replaceAll("[^A-Za-z0-9 ]","");
        String correct = clean.toLowerCase();
        this.getAllWithPrefix(correct);
        deletedvalues.addAll(this.prefixsort);
        Node header = this.get(correct);
        if (header == null){
            return null;
        }
        header.v.clear();;
        Arrays.fill(header.links, null);
        return deletedvalues;   
    }
    private boolean emptyChecker(Node[] array){
        boolean empty = true;
        if(array ==null){
            return empty;
        }
        for (int i=0; i<array.length; i++) {
            if (array[i] != null) {
                empty = false;
                break;
            }   
        }
        return empty;

    }
}    
