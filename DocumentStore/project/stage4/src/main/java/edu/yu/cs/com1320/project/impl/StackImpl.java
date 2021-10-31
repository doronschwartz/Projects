package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Stack;


public class StackImpl<T> implements Stack<T>{
    class Node<T>{
        T data;
        Node<T> next;
        Node(){
            
        }
        
    }
    private Node<T> head; 
    private int numelements;
    public StackImpl(){
        
    }
    
    public void push(T element){
        Node<T> midpoint = new Node<>() ; 
        midpoint.data = element;
        midpoint.next = this.head;
        head = midpoint;
        this.numelements++;
    }

    
    public T pop(){
        if(this.size() == 0){
            return null;
        }
        else{
            this.numelements--;
            T returndata = head.data;
            head = head.next;
            return returndata;
        }
    }

    public T peek(){
        if(this.size() == 0){
            return null;
        }
        return head.data;
    }

    public int size(){
        return this.numelements;
    }

}
