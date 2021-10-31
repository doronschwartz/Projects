package edu.yu.cs.com1320.project.stage2.impl;
import java.net.URI;
import java.util.*;
import edu.yu.cs.com1320.project.stage2.*;
import edu.yu.cs.com1320.project.impl.*;
import edu.yu.cs.com1320.project.*;
import java.io.*;
import java.net.URISyntaxException;
import java.util.function.Function;
public class DocumentStoreImpl implements DocumentStore{
    
    private HashTableImpl<URI,Document> storage;
    private Document document;
    private StackImpl<Command> commandStack;
    private Function<URI,Boolean> undo;
    public DocumentStoreImpl(){
        this.storage = new HashTableImpl<>();
        this.commandStack = new StackImpl<>();
    
    }
    
    public int putDocument(InputStream input, URI uri, DocumentFormat format) throws IOException{
        byte data[];
        if(format == null || uri == null ){
            throw new IllegalArgumentException();
        }
        Document current = this.storage.get(uri);
        this.commandStack.push(new Command(uri,(URI uri2)->{
            uri2 = uri;
           
            if (current == null){
                this.deletestack(uri2);
            }
            else{
                this.pushstack(uri2,current);
            }
            return true;
        }));            

        if (input == null){
            data = null;
            this.document = new DocumentImpl(uri,data); 
        }   
        else{
            data = input.readAllBytes();
            if (format == DocumentFormat.BINARY){
            this.document = new DocumentImpl(uri,data);
        }
            else if(format == DocumentFormat.TXT){
            this.document = new DocumentImpl(uri,new String(data));
        }
        
    } 
        Document placed = storage.put(uri, this.document);
        if (placed != null){
            return placed.hashCode();
        }
        else{
            return 0;
        }
    }
    
    public Document getDocument(URI uri){
        return storage.get(uri);
    }
    
    public boolean deleteDocument (URI uri){
        Document replacer = this.storage.get(uri);
        this.commandStack.push(new Command(uri,(URI uri2)->{
            uri2 = uri;
            this.pushstack(uri2,replacer);
            return true;
        }));           
        return storage.put(uri,null) != null;
     }
   public void undo(URI uri) throws IllegalStateException{
   if (this.commandStack.size()==0){
       throw new IllegalStateException();
   }
   
    Command top = this.commandStack.peek();    
    StackImpl<Command> temp = new StackImpl<>(); 
    while(top !=null){
        if(!(top.getUri().equals(uri))){
            temp.push(top);
            top = this.commandStack.pop();
        }
        else{
            break;
        }
    }
    if (top==null){
        for (int i = 0;i<=temp.size();i++){
            this.commandStack.push(temp.pop());
        }
        
    }
    top.undo();
    this.commandStack.pop();
    for (int i = 0;i<=temp.size();i++){
        this.commandStack.push(temp.pop());
    }
 }  
   public void undo() throws IllegalStateException{
    if (this.commandStack.size()==0){
        throw new IllegalStateException();
    }   
    Command top = this.commandStack.pop();
    top.undo();
} 
    
    private void deletestack(URI uri){
        this.storage.put(uri,null);
    }

    private void pushstack(URI uri, Document undoer){
        this.storage.put(uri,undoer);
    }
}
