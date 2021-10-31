package edu.yu.cs.com1320.project.impl;

import java.util.*;
import edu.yu.cs.com1320.project.*;
public class HashTableImpl<Key,Value> implements HashTable<Key,Value>{

    
    class Node<Key,Value>{
        Key k;
        Value v;
        Node<Key,Value> next;
        Node(Key k, Value v){
            this.k = k;
            this.v = v;
        }
        private Key getKey(){
            return this.k;
        }
        private Value getValue(){
            return this.v;
        }
    }
    
    private Node<Key,Value>storageArray[];
    private int numelements;
    public HashTableImpl(){
        this.storageArray = new Node[5];
}
    private int keytoIndex(Key k){
        return k.hashCode() % storageArray.length;
    }
    /**
    * @param k the key whose value should be returned
    * @return the value that is stored in the HashTable for k, or null if there is no such key in the table
    */
   @Override
    public Value get(Key k){
        int index = this.keytoIndex(k);
        // retrieve from the compressor
        Node<Key,Value> head =  this.storageArray[index];
        //retrieve from array of nodes   
        while(head != null){
            if (head.getKey().equals(k)){
                return head.getValue();
            }
            head = head.next;
        }
        return null;
    }

   
    /**
     * @param k the key at which to store the value
     * @param v the value to store.
     * To delete an entry, put a null value.
     * @return if the key was already present in the HashTable, return the previous value stored for the key. If the key was not already present, return null.
     */
    @Override
     public Value put(Key k, Value v){
         int index = this.keytoIndex(k);
         Node<Key, Value> first = this.storageArray[index];
         
         while(first != null){
            if(first.getKey().equals(k)){ 
                Value prev = first.v;
                first.v = v;
                return prev;    
            }
            first = first.next;
        }
    
        first = this.storageArray[index];
        
        if(v == null){
            Node<Key,Value> previous = null;
            
            while(first != null){
                if(first.getKey().equals(k)){
                    
                    break;
                }        
                previous = first;
                first = first.next;
            }  
            if(first == null){
                return null;
            }
            if(first.v !=null){
            Value prev = first.v;
            previous.next = first.next;
            return prev;
            }
            else{
                return null;
            }
        }

        else{
        Node<Key,Value> newlink = new Node<Key,Value>(k,v);
        newlink.next = first;
        storageArray[index] = newlink;
        int prevlength = storageArray.length;
        if(prevlength < this.numelements/4){
            Node<Key,Value> previous[] = this.storageArray; 
            this.storageArray = new Node[prevlength*2];
            for (int i = 0;i<previous.length;i++){
                Node<Key,Value>top = previous[i];
                while(top != null){
                    this.put(top.k,top.v);
                    top = top.next;
                }
            }
        }
        this.numelements++;
    }
    return null;
    }
}