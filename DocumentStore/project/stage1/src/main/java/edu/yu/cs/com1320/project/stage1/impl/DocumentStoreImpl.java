package edu.yu.cs.com1320.project.stage1.impl;
import java.net.URI;
import java.util.*;
import edu.yu.cs.com1320.project.stage1.*;
import edu.yu.cs.com1320.project.impl.*;
import java.io.*;
import java.net.URISyntaxException;
public class DocumentStoreImpl implements DocumentStore{
    
    private HashTableImpl<URI,Document> storage;
    private Document document;
    public DocumentStoreImpl(){
        this.storage = new HashTableImpl<>();
    }
    
    public int putDocument(InputStream input, URI uri, DocumentFormat format) throws IOException{
        byte data[];
        if(format == null || uri == null ){
            throw new IllegalArgumentException();
        }
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
      return storage.put(uri,null) != null;
     }
     
}
