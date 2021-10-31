package edu.yu.cs.com1320.project.stage4.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.impl.*;
import edu.yu.cs.com1320.project.stage4.*;
import edu.yu.cs.com1320.project.stage4.impl.DocumentImpl;

public class DocumentStoreImpl implements DocumentStore{
    private HashTableImpl<URI,Document> storage;
    private DocumentImpl document;
    private StackImpl<Undoable> commandStack;
    private TrieImpl<Document> trie;
    private MinHeapImpl<Document> minheap;
    private int numberofdocs = 0;
    private int memory = 0;
    private int fulldoc = Integer.MAX_VALUE;
    private int fullmem = Integer.MAX_VALUE;
    
    public DocumentStoreImpl(){
        this.storage = new HashTableImpl<>();
        this.commandStack = new StackImpl<>();
        this.trie = new TrieImpl<>();
        this.minheap = new MinHeapImpl<Document>();
        }
    public int putDocument(InputStream input, URI uri, DocumentFormat format) throws IOException {
        byte data[];
        if(format == null || uri == null ){
            throw new IllegalArgumentException();
        }
        Document current = this.storage.get(uri);
        if(current != null){
            for(String s: current.getWords()){
                this.trie.delete(s, current);
            }
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
                    for(String s: this.document.getWords()){
                        this.trie.put(s, this.document);
                }
                this.memory += this.memoryReturner(this.document); 
            }
            this.numberofdocs++;
        } 
        this.document.setLastUseTime(System.nanoTime());
        
        this.storageManagement();
        
        this.minheap.insert(this.document);
        Document trieheap = this.document;
        GenericCommand<URI> reverseputs = new GenericCommand<URI>(uri,(URI uri2)->{
            uri2 = uri;
            this.numberofdocs--;
            this.memory -= this.memoryReturner(trieheap);
            this.deleteHeap(trieheap);
            for(String s:trieheap.getWords()){
                this.trie.delete(s, trieheap);
            }
            if (current == null){
                this.deletestack(uri2);    
            }
            else{
                this.pushstack(uri2,current);
                for(String s: current.getWords()){
                    this.trie.put(s, current);
                }
            }

            return true;
             });
            
            this.commandStack.push(reverseputs);
            Document placed = storage.put(uri, this.document);
            if (placed != null){
                return placed.hashCode();
            }
            else{
                return 0;
            }
    }

    @Override
    public Document getDocument(URI uri) {
        if(storage.get(uri) != null){
            storage.get(uri).setLastUseTime(System.nanoTime());
            this.minheap.reHeapify(storage.get(uri)); 
            return storage.get(uri);
        }
        else{
            return null;
        }        
        
    }

    @Override
    public boolean deleteDocument(URI uri) {
        Document replacer = this.getDocument(uri);
        boolean checker = this.storage.put(uri,null)!=null;
        if(checker == false){
            return checker;
        }
        if(replacer != null){
            for(String s:replacer.getWords()){
                this.trie.delete(s,replacer);
            }
        }
        //replacer.setLastUseTime(System.nanoTime());
        this.deleteHeap(replacer);
        this.numberofdocs--;
        this.memory -= this.memoryReturner(replacer);
        GenericCommand<URI> reversedelete = new GenericCommand<URI>(uri, (URI uri2)->{
            uri2 = uri;
            this.storage.put(uri,replacer);
            this.numberofdocs++;
            this.memory += this.memoryReturner(replacer);
            this.storageManagement();
            replacer.setLastUseTime(System.nanoTime());
            this.minheap.insert(replacer);
            if(replacer != null){
            for(String s:replacer.getWords()){
                    this.trie.put(s,replacer);
                }
            }
            return true;
        });
        this.commandStack.push(reversedelete);
        return checker;
    }

    @Override
    public void undo() throws IllegalStateException {
        if (this.commandStack.size()==0){
            throw new IllegalStateException();
        }   
        if(this.commandStack.peek() instanceof CommandSet){
            CommandSet<URI> top = (CommandSet<URI>)this.commandStack.pop();
            top.undoAll();
        }
        else{
            GenericCommand<URI> top = (GenericCommand<URI>) this.commandStack.pop();
            top.undo();
        }
        
    }

    @Override
    public void undo(URI uri) throws IllegalStateException {
        if (this.commandStack.size()==0){
            throw new IllegalStateException();
        }   
          StackImpl<Undoable> temp = new StackImpl<>();
        while (this.commandStack.peek() != null){
            if(this.commandStack.peek() instanceof CommandSet){
                CommandSet<URI> top = (CommandSet<URI>)this.commandStack.peek();
                if(top.containsTarget(uri)){
                    top.undo(uri);
                    if(top.size() != 0){
                        temp.push(top);
                    }
                    this.commandStack.pop();
                    
                    break;
                }
                else{
                    temp.push(top);
                    this.commandStack.pop();
                }
            }
            else{
                GenericCommand<URI> top = (GenericCommand<URI>)this.commandStack.peek();
                if(top.getTarget().equals(uri)){
                    try{
                    top.undo();
                    }
                    catch(IllegalStateException e){
                        for (int i = 0;i<=temp.size();i++){
                            this.commandStack.push(temp.pop());
                        }
                        throw new IllegalStateException();
                    }
                    break;
                }
                else{
                    temp.push(top);
                    this.commandStack.pop();
                } 
            }
        }
        if(this.commandStack.peek() == null){
           new IllegalStateException();
        }
        if(temp.peek() == null){
            return;
        }
        for (int i = 0;i<=temp.size();i++){
            this.commandStack.push(temp.pop());
        }
        
    }

    @Override
    public List<Document> search(String keyword) {
        
        List<Document> documents = this.trie.getAllSorted(keyword, (Document1,Document2)->{
            if(((DocumentImpl)(Document1)).wordCount(keyword) < ((DocumentImpl)(Document2)).wordCount(keyword)){
                return 1;
            }
            else if (((DocumentImpl)(Document1)).wordCount(keyword) > ((DocumentImpl)(Document2)).wordCount(keyword)){
                return -1;
            }
            return 0;
        }); 
        long setTime = System.nanoTime() ;
        for(Document d: documents){
            d.setLastUseTime(setTime);
            this.minheap.reHeapify(d);
        }
        return documents;
    }

    @Override
    public List<Document> searchByPrefix(String keywordPrefix) {
        List<Document> prefixes = this.trie.getAllWithPrefixSorted(keywordPrefix,(Document1,Document2)->{
            int count1 = 0;
            int count2 = 0;
            for(String s:Document1.getWords()){
                if(s.indexOf(keywordPrefix) == 0){
                    count1 += Document1.wordCount(s);
                }
            }
            for(String s:Document2.getWords()){
                if(s.indexOf(keywordPrefix) == 0){
                    count2 += Document2.wordCount(s);
                }
            }
            if(count1 < count2){
                return 1;
            }
            else if (count1 > count2){
                return -1;
            }
            return 0;
        });
        long setTime = System.nanoTime();
        for(Document d: prefixes){
            d.setLastUseTime(setTime);
            this.minheap.reHeapify(d);
        }
        return prefixes;
    }

    @Override
    public Set<URI> deleteAll(String keyword) {
        Set<Document> deleted = this.trie.deleteAll(keyword);
        Set<URI> uris = new HashSet<>();
        if(deleted == null){
            return uris;
        }
        for(Document d:deleted){
            this.storage.put(d.getKey(),null);
            uris.add(((DocumentImpl) d).getKey());
                for(String s:((DocumentImpl) d).getWords()){
                if(this.search(s).contains(d)){
                    this.trie.delete(s, d);
                }
            }
            this.memory -= this.memoryReturner(d);
            this.numberofdocs--;
            this.deleteHeap(d);
        }
        CommandSet<URI> reversedeleteAll  = new CommandSet<>();
        for(Document d: deleted){
            GenericCommand<URI> undeleted = new GenericCommand<URI>(d.getKey(), (URI uri2)->{
                uri2 = d.getKey();
                this.memory += this.memoryReturner(d);
                this.numberofdocs++;
                this.storageManagement();
                d.setLastUseTime(System.nanoTime());
                this.minheap.insert(d);
                this.storage.put(uri2,d);
                if(d.getWords().contains(keyword)){
                    for(String s: d.getWords()){
                        this.trie.put(s, d);
                    }
                }
            return true;
        });
        reversedeleteAll.addCommand(undeleted);    
    }
        this.commandStack.push(reversedeleteAll);
        return uris;
    }

    @Override
    public Set<URI> deleteAllWithPrefix(String keywordPrefix) {
        Set<Document> deleted = this.trie.deleteAllWithPrefix(keywordPrefix);
        Set<URI> uris = new HashSet<>();
        if(deleted == null){
            return uris;
        }
        if(deleted.size()==0){
            return uris;
        }
        for(Document d:deleted){
            uris.add(d.getKey());
            this.storage.put(d.getKey(),null);
            for(String s:((DocumentImpl) d).getWords()){
                this.trie.delete(s, d);
            }
            this.memory -= this.memoryReturner(d);
            this.numberofdocs--;
            this.deleteHeap(d);
        }
        CommandSet<URI> reversedeleteAll  = new CommandSet<>();
        for(Document d: deleted){
            GenericCommand<URI> undeleted = new GenericCommand<URI>(d.getKey(), (URI uri2)->{
                uri2 = d.getKey();
                this.memory += this.memoryReturner(d);
                this.numberofdocs++;
                this.storageManagement();
                this.storage.put(uri2,d);
                for(String s: d.getWords()){
                        this.trie.put(s, d);
                    }
                d.setLastUseTime(System.nanoTime());
                this.minheap.insert(d);
                return true;
        });
        reversedeleteAll.addCommand(undeleted);
    }
    this.commandStack.push(reversedeleteAll);   
    return uris;
    }

    @Override
    public void setMaxDocumentCount(int limit) {
        this.fulldoc = limit;
        this.storageManagement();
    }
    @Override
    public void setMaxDocumentBytes(int limit) {
        this.fullmem = limit;    
        this.storageManagement();   
    }
    private void storageManagement(){
        while(this.numberofdocs > this.fulldoc  || this.memory > this.fullmem){
            Document forcedelete = (DocumentImpl)this.minheap.remove();
            this.storage.put(forcedelete.getKey(),null);
            // go through the stack generic command straight up pop, coomand set use iterator and dig through 
            StackImpl<Undoable> temp = new StackImpl<>(); 
            for(int i = 0; i < this.commandStack.size();i++){
                if(this.commandStack.peek() instanceof CommandSet){
                    CommandSet<URI> commands = (CommandSet<URI>)this.commandStack.peek();
                    Iterator<GenericCommand<URI>> gciterator =  commands.iterator();
                    while(gciterator.hasNext()){
                        if((gciterator.next().getTarget().equals(forcedelete.getKey()))){
                           gciterator.remove(); 
                        }
                    }
                    temp.push(this.commandStack.pop());
                }
                else{
                    GenericCommand<URI> top = (GenericCommand<URI>)this.commandStack.peek();
                    if(!(top.getTarget().equals(forcedelete.getKey()))){
                        temp.push(top);
                    }
                    this.commandStack.pop();
                    
                }
        }   
        this.commandStack =temp;    
        if (forcedelete.getWords().size() != 0){
                   for(String s: forcedelete.getWords()){
                       this.trie.delete(s,forcedelete);
                   }
               }               
               this.memory -= this.memoryReturner(forcedelete);
               this.numberofdocs--;
        }
    }   
    private void deletestack(URI uri){
        this.storage.put(uri,null);
    }

    private void pushstack(URI uri, Document undoer){
        this.storage.put(uri,undoer);
    }
    private int memoryReturner(Document d){
        int docmemory = 0; 
        if(d == null){
            return docmemory;
        }
        if(d.getDocumentBinaryData() == null){
            docmemory = d.getDocumentTxt().getBytes().length; 
        }
        else{
            docmemory = d.getDocumentBinaryData().length;
        }
        return docmemory;
    }
    private void deleteHeap(Document d){
        boolean travesed = false;
        MinHeapImpl<Document> temp = new MinHeapImpl<>();
        while(travesed == false){
            try{
                Document top = (Document) this.minheap.remove();
                if(!(top.equals(d))){
                    top.setLastUseTime(System.nanoTime());
                }
                temp.insert(top);
            }
            catch(NoSuchElementException e){
                travesed = true;
            }
           
        }
        this.minheap = temp;
        this.minheap.remove();
    }
}
    
    
