package edu.yu.cs.com1320.project.stage3.impl;
import java.util.*;
import java.net.URI;
import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.impl.*;
import edu.yu.cs.com1320.project.stage3.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage3.Document;
import edu.yu.cs.com1320.project.stage3.DocumentStore;
import java.io.*;
public class DocumentStoreImpl implements DocumentStore {
    private HashTableImpl<URI,Document> storage;
    private DocumentImpl document;
    private StackImpl<Undoable> commandStack;
    private TrieImpl<Document> trie;
    public DocumentStoreImpl(){
        this.storage = new HashTableImpl<>();
        this.commandStack = new StackImpl<>();
        this.trie = new TrieImpl<>();
    }
    public int putDocument(InputStream input, URI uri, DocumentFormat format) throws IOException{
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
         // create undo, should be a straight up delete of the hashtable value and the trie value;
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
                
            }
        } 
        Document trieplaced = this.document;
        GenericCommand<URI> reverseputs = new GenericCommand<URI>(uri,(URI uri2)->{
        uri2 = uri;
        for(String s:trieplaced.getWords()){
            this.trie.delete(s, trieplaced);
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
    

    /**
     * @param uri the unique identifier of the document to get
     * @return the given document
     */
     public Document getDocument(URI uri){
        if(storage.get(uri) != null){
            return storage.get(uri);
        }
        else{
            return null;
        }
    }

    /**
     * @param uri the unique identifier of the document to delete
     * @return true if the document is deleted, false if no document exists with that URI
     */
    public boolean deleteDocument(URI uri){
        Document replacer = this.getDocument(uri);
        boolean checker = this.storage.put(uri,null)!=null;
        if(replacer != null){
        for(String s:replacer.getWords()){
            this.trie.delete(s,replacer);
        }
        }
        GenericCommand<URI> reversedelete = new GenericCommand<URI>(uri, (URI uri2)->{
            uri2 = uri;
            this.storage.put(uri,replacer);
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

    /**
     * undo the last put or delete command
     * @throws IllegalStateException if there are no actions to be undone, i.e. the command stack is empty
     */
    public void undo() throws IllegalStateException{
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

    /**
     * undo the last put or delete that was done with the given URI as its key
     * @param uri
     * @throws IllegalStateException if there are no actions on the command stack for the given URI
     */
    public void undo(URI uri) throws IllegalStateException{
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

    
    /**
     * Retrieve all documents whose text contains the given keyword.
     * Documents are returned in sorted, descending order, sorted by the number of times the keyword appears in the document.
     * Search is CASE INSENSITIVE.
     * @param keyword
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    public List<Document> search(String keyword){
        return this.trie.getAllSorted(keyword, (Document1,Document2)->{
            if(((DocumentImpl)(Document1)).wordCount(keyword) < ((DocumentImpl)(Document2)).wordCount(keyword)){
                return 1;
            }
            else if (((DocumentImpl)(Document1)).wordCount(keyword) > ((DocumentImpl)(Document2)).wordCount(keyword)){
                return -1;
            }
            return 0;
        }); 
    }

    /**
     * Retrieve all documents whose text starts with the given prefix
     * Documents are returned in sorted, descending order, sorted by the number of times the prefix appears in the document.
     * Search is CASE INSENSITIVE.
     * @param keywordPrefix
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    public List<Document> searchByPrefix(String keywordPrefix){
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
        return prefixes;
    }

    /**
     * Completely remove any trace of any document which contains the given keyword
     * @param keyword
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
     public Set<URI> deleteAll(String keyword){
        Set<Document> deleted = this.trie.deleteAll(keyword);
        Set<URI> uris = new HashSet<>();
        if(deleted.size() == 0){
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
        }
        CommandSet<URI> reversedeleteAll  = new CommandSet<>();
        for(Document d: deleted){
            GenericCommand<URI> undeleted = new GenericCommand<URI>(d.getKey(), (URI uri2)->{
                uri2 = d.getKey();
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

    /**
     * Completely remove any trace of any document which contains a word that has the given prefix
     * Search is CASE INSENSITIVE.
     * @param keywordPrefix
     * @return a Set of URIs of the documents that were deleted.
     */
     public Set<URI> deleteAllWithPrefix(String keywordPrefix){
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
                if(this.search(s).contains(d)){
                    this.trie.delete(s, d);
                }
            }
        }
        CommandSet<URI> reversedeleteAll  = new CommandSet<>();
        for(Document d: deleted){
            GenericCommand<URI> undeleted = new GenericCommand<URI>(d.getKey(), (URI uri2)->{
                uri2 = d.getKey();
                this.storage.put(uri2,d);
                for(String s: d.getWords()){
                        this.trie.put(s, d);
                    }
                
            return true;
        });
        reversedeleteAll.addCommand(undeleted);
    }
    this.commandStack.push(reversedeleteAll);   
    return uris;
}
     private void deletestack(URI uri){
        this.storage.put(uri,null);
    }

    private void pushstack(URI uri, Document undoer){
        this.storage.put(uri,undoer);
    }
    
    }

