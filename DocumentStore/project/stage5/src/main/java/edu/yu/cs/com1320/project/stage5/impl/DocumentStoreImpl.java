package edu.yu.cs.com1320.project.stage5.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.impl.*;
import edu.yu.cs.com1320.project.stage5.*;
import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;

public class DocumentStoreImpl implements DocumentStore{
    private BTreeImpl<URI,Document> storage;
    private DocumentImpl document;
    private StackImpl<Undoable> commandStack;
    private TrieImpl<URI> trie;
    private MinHeapImpl<TimeStorer> minheap;
    private Map<URI,TimeStorer> TimeStorermap;
    private int numberofdocs = 0;
    private int memory = 0;
    private int fulldoc = Integer.MAX_VALUE;
    private int fullmem = Integer.MAX_VALUE;
    private File baseDir = null;
    
    private class TimeStorer implements Comparable<TimeStorer> {
        private URI uri;
        private BTreeImpl<URI,Document> btree;
        public TimeStorer (URI uri, BTreeImpl<URI, Document> btree) {
            this.uri = uri;
            this.btree = btree;
        }
        public URI getKey(){
            return uri;
        }
        @Override
        public int compareTo(TimeStorer o) {
            if(o == null){
                return 1;
            }
            else if( this.btree.get(uri) == null){
                return -1;
            }
            else if(o.btree.get(o.getKey()) == null){
                return 1;
            }
            else if (this.btree.get(uri).getLastUseTime() < (o.btree.get(o.getKey())).getLastUseTime()){
                return -1;
            } 
            else if(this.btree.get(uri).getLastUseTime() > this.btree.get(o.getKey()).getLastUseTime()){
                return 1;
            }
            
            return 0;
            
            
           
        }
        
    }
    
    
    
    
    public DocumentStoreImpl(){
        this.storage = new BTreeImpl<>();
        this.commandStack = new StackImpl<>();
        this.trie = new TrieImpl<>();
        this.minheap = new MinHeapImpl<>();
        this.TimeStorermap = new HashMap<>();
        this.storage.setPersistenceManager(new DocumentPersistenceManager(null));
        }
    public DocumentStoreImpl(File baseDir){
        this.storage = new BTreeImpl<>();
        this.commandStack = new StackImpl<>();
        this.trie = new TrieImpl<>();
        this.minheap = new MinHeapImpl<>();
        this.TimeStorermap = new HashMap<>();
        this.baseDir = baseDir;
        this.storage.setPersistenceManager(new DocumentPersistenceManager(baseDir));
        }
        
    public int putDocument(InputStream input, URI uri, DocumentFormat format) throws IOException {
        byte data[];
        if(format == null || uri == null ){
            throw new IllegalArgumentException();
        }
        Document current = this.storage.get(uri);
        if(current != null){
            for(String s: current.getWords()){
                this.trie.delete(s, current.getKey());
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
                    this.memory += this.document.getDocumentBinaryData().length; 
                }
                else if(format == DocumentFormat.TXT){
                    this.document = new DocumentImpl(uri,new String(data));
                    for(String s: this.document.getWords()){
                        this.trie.put(s, this.document.getKey());
                    }
                this.memory += this.document.getDocumentTxt().getBytes().length; 
                   
                }
                
                
            
            this.numberofdocs++;
        } 
        this.document.setLastUseTime(System.nanoTime());
        Document destroyed = null;
        try {
             destroyed = this.storageManagement();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Document actualdestroyed = destroyed;
        TimeStorer currentdoc = new TimeStorer(uri,this.storage);
        this.TimeStorermap.put(uri,currentdoc);
        Document placed = storage.put(uri, this.document);
        this.minheap.insert(currentdoc);
        Document trieheap = this.document;
        
        GenericCommand<URI> reverseputs = new GenericCommand<URI>(uri,(URI uri2)->{
            uri2 = uri;
            this.numberofdocs--;
            if(trieheap.getDocumentBinaryData() == null){
                this.memory -= trieheap.getDocumentTxt().getBytes().length;
            }
            else{
                this.memory -= trieheap.getDocumentBinaryData().length;
            }
            for(String s:trieheap.getWords()){
                this.trie.delete(s, trieheap.getKey());
            }
           if(actualdestroyed != null){
                this.storage.get(actualdestroyed.getKey());
                actualdestroyed.setLastUseTime(System.nanoTime());
                this.minheap.insert(this.TimeStorermap.get(actualdestroyed.getKey()));
                for(String s: actualdestroyed.getWords()){
                    this.trie.put(s, actualdestroyed.getKey());
                }
                if(trieheap.getDocumentBinaryData() == null){
                    this.memory += actualdestroyed.getDocumentTxt().getBytes().length;
                }
                else{
                    this.memory += actualdestroyed.getDocumentBinaryData().length;
                }
            }
            
         
            if (current != null){
                this.storage.put(uri2,current);
                for(String s: current.getWords()){
                    this.trie.put(s, current.getKey());
                }
            }
           else{
               this.storage.put(uri,null);
           }
            return true;
             });
            
            this.commandStack.push(reverseputs);
            if (placed != null){
                return placed.hashCode();
            }
            else{
                return 0;
            }

    }

    @Override
    public Document getDocument(URI uri) {
        Path directory = this.pathreturner(uri);
        
        if(Files.exists(directory)){
            Document returned = this.storage.get(uri);
            this.numberofdocs++;
            if(returned.getDocumentBinaryData() == null){
                this.memory += this.document.getDocumentTxt().getBytes().length;
            }
            else{
                this.memory += this.document.getDocumentBinaryData().length;
            }
            
            try {
                this.storageManagement();
            } catch (URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            this.minheap.insert(this.TimeStorermap.get(uri));
            return returned;
        }
        else{   
            Document returned = this.storage.get(uri);
            if(returned != null){
                returned.setLastUseTime(System.nanoTime());    
                this.minheap.reHeapify(this.TimeStorermap.get(uri));
            }
            else{
                return null;
            }
            
            return returned;
        }
              
        
    }

    @Override
    public boolean deleteDocument(URI uri) {
        Document replacer = this.getDocument(uri);
        this.deleteHeap(this.TimeStorermap.get(uri));
        boolean checker = this.storage.put(uri,null)!=null;
        
        if(checker == false){
            return checker;
        }
        if(replacer != null){
            for(String s:replacer.getWords()){
                this.trie.delete(s,replacer.getKey());
            }
        }
        
        
        this.numberofdocs--;
        if(replacer.getDocumentBinaryData() == null){
            this.memory -= replacer.getDocumentTxt().getBytes().length;
        }
        else{
            this.memory -= replacer.getDocumentBinaryData().length;
        }
        GenericCommand<URI> reversedelete = new GenericCommand<URI>(uri, (URI uri2)->{
            uri2 = uri;
            
            this.numberofdocs++;
            if(replacer.getDocumentBinaryData() == null){
                this.memory += replacer.getDocumentTxt().getBytes().length;
            }
            else{
                this.memory += replacer.getDocumentBinaryData().length;
            }
            try {
                this.storageManagement();
            } catch (URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            replacer.setLastUseTime(System.nanoTime());
            this.storage.put(uri,replacer);
            this.minheap.insert(this.TimeStorermap.get(uri2));
            if(replacer != null){
            for(String s:replacer.getWords()){
                    this.trie.put(s,uri);
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
        
        List<URI> uris = this.trie.getAllSorted(keyword, (uri1,uri2)->{
            if(this.storage.get(uri1).wordCount(keyword) < this.storage.get(uri2).wordCount(keyword)){
                return 1;
            }
            else if (this.storage.get(uri1).wordCount(keyword) > this.storage.get(uri2).wordCount(keyword)){
                return -1;
            }
            return 0;
        }); 
        long setTime = System.nanoTime() ;
        List<Document> documents = new ArrayList<Document>();
        for(URI uri: uris){
            Path uripath = this.pathreturner(uri);
            if(Files.exists(uripath)){
                Document gotten = this.storage.get(uri);
                this.numberofdocs++;
                this.memory += this.memoryReturner(uri);
                try {
                    this.storageManagement();
                } catch (URISyntaxException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            Document returned = this.storage.get(uri);
            returned.setLastUseTime(System.nanoTime());
            
            this.minheap.reHeapify(this.TimeStorermap.get(uri));
            documents.add(this.storage.get(uri));
        }
        return documents;
    }

    @Override
    public List<Document> searchByPrefix(String keywordPrefix) {
        List<URI> prefixes = this.trie.getAllWithPrefixSorted(keywordPrefix,(uri1,uri2)->{
            int count1 = 0;
            int count2 = 0;
            Set<String> words1 = this.storage.get(uri1).getWords();
            for(String s:words1){
                if(s.indexOf(keywordPrefix) == 0){
                    count1 += this.storage.get(uri1).wordCount(s);
                }
            }
            Set<String> words2 = this.storage.get(uri2).getWords();
            for(String s:words2){
                if(s.indexOf(keywordPrefix) == 0){
                    count2 += this.storage.get(uri2).wordCount(s);
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
        List<Document> prefix = new ArrayList<>();
        for(URI uri: prefixes){
            Path uripath = this.pathreturner(uri);
            if(Files.exists(uripath.getParent())){
                this.numberofdocs++;
                this.memory += this.memoryReturner(uri);
                try {
                    this.storageManagement();
                } catch (URISyntaxException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            Document returned = this.storage.get(uri);
            returned.setLastUseTime(System.nanoTime());
            this.storage.put(uri, returned);
            this.minheap.reHeapify(this.TimeStorermap.get(uri));
            prefix.add(this.storage.get(uri));
        }
        return prefix;
    }

    @Override
    public Set<URI> deleteAll(String keyword) {
        Set<URI> deleted = this.trie.deleteAll(keyword);
        Set<URI> uris = new HashSet<>();
        Map<URI,Document> deletedmap = new HashMap<>();
        if(deleted == null){
            return uris;
        }
        for(URI uri:deleted){
            uris.add(uri);
            Set<String> words = this.storage.get(uri).getWords(); 
            for(String s:words){
                this.trie.delete(s, uri);
            }   
            Document fordeleting = this.storage.get(uri);
            deletedmap.put(uri, fordeleting);
            if(fordeleting.getDocumentBinaryData() == null){
                this.memory -= fordeleting.getDocumentTxt().getBytes().length;
            }
            else{
                this.memory -= fordeleting.getDocumentBinaryData().length;
            }
            this.numberofdocs--;
            this.deleteHeap(this.TimeStorermap.get(uri));
            this.storage.put(uri,null);
        }
        CommandSet<URI> reversedeleteAll  = new CommandSet<>();
        for(URI uri: deleted){
            GenericCommand<URI> undeleted = new GenericCommand<URI>(uri, (URI uri2)->{
                uri2 = uri;
                Document undeletedDoc = deletedmap.get(uri2);
                if(undeletedDoc.getDocumentBinaryData() == null){
                    this.memory -= undeletedDoc.getDocumentTxt().getBytes().length;
                }
                else{
                    this.memory -= undeletedDoc.getDocumentBinaryData().length;
                }
                this.numberofdocs++;
                undeletedDoc.setLastUseTime(System.nanoTime());
                this.storage.put(undeletedDoc.getKey(),undeletedDoc);
                this.minheap.insert(this.TimeStorermap.get(uri));
                for(String s: undeletedDoc.getWords()){
                        this.trie.put(s, uri);
                    }
            return true;
        });
        reversedeleteAll.addCommand(undeleted);    
        }
        this.commandStack.push(reversedeleteAll);
        return deleted;
    }

    @Override
    public Set<URI> deleteAllWithPrefix(String keywordPrefix) {
        Set<URI> deleted = this.trie.deleteAllWithPrefix(keywordPrefix);
        Set<URI> uris = new HashSet<>();
        Map<URI,Document> deletedmap = new HashMap<>();
        if(deleted == null){
            return uris;
        }
        if(deleted.size()==0){
            return uris;
        }
        for(URI uri:deleted){
            uris.add(uri);
            Document fordeleting = this.storage.get(uri);
            Set<String> words = fordeleting.getWords(); 
            for(String s:words){
                this.trie.delete(s, uri);
            }   
            this.memory -= this.memoryReturner(uri);
            this.numberofdocs--;
            deletedmap.put(uri,fordeleting);
            this.deleteHeap(this.TimeStorermap.get(uri));
            this.storage.put(uri, null);
        }
        CommandSet<URI> reversedeleteAll  = new CommandSet<>();
        for(URI uri: deleted){
            GenericCommand<URI> undeleted = new GenericCommand<URI>(uri, (URI uri2)->{
                uri2 = uri;
                Document undeletedDoc = deletedmap.get(uri2);
                if(undeletedDoc.getDocumentBinaryData() == null){
                    this.memory -= undeletedDoc.getDocumentTxt().getBytes().length;
                }
                else{
                    this.memory -= undeletedDoc.getDocumentBinaryData().length;
                }
                this.numberofdocs++;
               
                for(String s: undeletedDoc.getWords()){
                        this.trie.put(s, uri2);
                }
                undeletedDoc.setLastUseTime(System.nanoTime());
                this.storage.put(undeletedDoc.getKey(),undeletedDoc);
                this.minheap.insert(this.TimeStorermap.get(uri));
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
        try {
            this.storageManagement();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    @Override
    public void setMaxDocumentBytes(int limit) {
        this.fullmem = limit;    
        try {
            this.storageManagement();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }   
    }
    private Document storageManagement() throws URISyntaxException {
        boolean cycle = false;
        Document fordeleting = null;
        while((this.numberofdocs > this.fulldoc  || this.memory > this.fullmem) && !(cycle)){
            cycle = false;
            TimeStorer forcedelete = (TimeStorer)this.minheap.remove();
            fordeleting = this.storage.get(forcedelete.getKey());
            
            // go through the stack generic command straight up pop, coomand set use iterator and dig through 
           
            if (fordeleting.getWords().size() != 0){
                Set<String> words = fordeleting.getWords();   
                for(String s: words){
                       this.trie.delete(s,forcedelete.getKey());
                   }
               }               
               this.memory -= this.memoryReturner(forcedelete.getKey());
               this.numberofdocs--;
               try{
                    this.storage.moveToDisk(forcedelete.getKey());
                }
                catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            cycle = true;
        }
        return fordeleting;
    }   
   
    private int memoryReturner(URI uri){
        int docmemory = 0; 
        if(uri == null){
            return docmemory;
        }
        
        
        if(this.storage.get(uri).getDocumentBinaryData() == null){
            docmemory = this.storage.get(uri).getDocumentTxt().getBytes().length; 
        }
        else{
            docmemory = this.storage.get(uri).getDocumentBinaryData().length;
        }
        return docmemory;
    }
    private void deleteHeap(TimeStorer m){
        boolean traversed = false;
        MinHeapImpl<TimeStorer> temp = new MinHeapImpl<>();
        while(traversed == false){
            try{
                TimeStorer top = (TimeStorer) this.minheap.remove();
                if(!(top.getKey().equals(m.getKey()))){
                     Document returned = this.storage.get(m.getKey());
                     returned.setLastUseTime(System.nanoTime());
                     temp.insert(top);
                }
               
            }
            catch(NoSuchElementException e){
                traversed = true;
            }
           
        }
        this.minheap = temp;
        
    }
    private Path pathreturner(URI uri){
        String rawScheme = uri.getRawSchemeSpecificPart() + ".json"; 
        if (rawScheme.startsWith("/")){
            rawScheme = rawScheme.substring(2);
        }
        
        String base; 
        if (this.baseDir == null){
            base = System.getProperty("user.dir");
        }
        else{
            base = this.baseDir.getAbsolutePath();
        }
        File complete = new File(base,rawScheme);
        Path path = Paths.get(complete.getAbsolutePath());
        return path;

    }
}
