package edu.yu.cs.com1320.project.impl;

import java.util.NoSuchElementException;

import edu.yu.cs.com1320.project.MinHeap;

public class MinHeapImpl<E extends Comparable<E>> extends MinHeap{

    
    public MinHeapImpl(){
        this.elements = new Comparable[2];
    }
    @Override
    public void reHeapify(Comparable element) {
        int arrayindex = this.getArrayIndex(element);
        if(this.elements.length<=2){
            return;
        }
        if(!(arrayindex >= (this.elements.length / 2))){
            
            if(this.isGreater(arrayindex, arrayindex*2) || this.isGreater(arrayindex, (arrayindex * 2)+1)){
                if(this.elements[arrayindex*2] != null && this.elements[(arrayindex*2)+1] != null){
                    
                    if(!(this.isGreater(arrayindex*2,(arrayindex * 2)+1))){
                        this.upHeap(arrayindex*2);
                        
                    }
                    else{
                        this.upHeap((arrayindex*2)+1);
                        
                    }
                }
                else{
                    if(this.elements[(arrayindex*2)+1] != null){
                        this.upHeap((arrayindex*2)+1);
                        
                    }
                    else if(this.elements[(arrayindex*2)] != null){
                        this.upHeap((arrayindex*2));
                        
                    }    
                }
            }

        } 
        else{
            if(arrayindex % 2 == 0){
                if(this.isGreater(arrayindex / 2, arrayindex)){                    
                    this.upHeap(arrayindex);
                }   
            }
            else{
                if(this.isGreater((arrayindex -1) / 2, arrayindex)){
                    this.upHeap(arrayindex);
                }
            }
        }
    }

    @Override
    protected int getArrayIndex(Comparable element) {
        for(int i = 0;i< this.elements.length;i++){
            if(this.elements[i]!=null){
                if (this.elements[i].equals(element)){
                    return i;
                }
            }
        }
        throw new NoSuchElementException();
    }

    @Override
    protected void doubleArraySize() {
        Comparable[] previous = this.elements;
        this.elements = new Comparable[previous.length * 2];
            for (int i = 0;i<previous.length;i++){
                Comparable temp = previous[i];
                this.elements[i] = temp;
                }
            }
        
    
        }

