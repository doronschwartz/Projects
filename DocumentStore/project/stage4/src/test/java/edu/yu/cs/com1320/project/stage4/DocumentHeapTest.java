package edu.yu.cs.com1320.project.stage4;



import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.impl.*;
import edu.yu.cs.com1320.project.stage4.*;
import edu.yu.cs.com1320.project.stage4.impl.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class DocumentHeapTest {
    //variables to hold possible values for doc1
    private URI uri1;
    private String txt1;

    //variables to hold possible values for doc2
    private URI uri2;
    private String txt2;

    //variables to hold possible values for doc3
    private URI uri3;
    private String txt3;

    //variables to hold possible values for doc4
    private URI uri4;
    private String txt4;

    @BeforeEach
    public void init() throws Exception {
        //init possible values for doc1
        this.uri1 = new URI("http://edu.yu.cs/com1320/project/doc1");
        this.txt1 = "the text of doc1, in plain text. No fancy file format - just plain old String. Computer. Headphones.";

        //init possible values for doc2
        this.uri2 = new URI("http://edu.yu.cs/com1320/project/doc2");
        this.txt2 = "Text for doc2. A plain old String.";

        //init possible values for doc3
        this.uri3 = new URI("http://edu.yu.cs/com1320/project/doc3");
        this.txt3 = "the text of doc3, this is";

        //init possible values for doc4
        this.uri4 = new URI("http://edu.yu.cs/com1320/project/doc4");
        this.txt4 = "This is the text of doc4";
    }
    @Test
    public void MinheapTest(){
    MinHeap<Document> minHeap =  new MinHeapImpl<>();
    DocumentImpl doc1 = new DocumentImpl(this.uri1,this.txt1);
    doc1.setLastUseTime(System.nanoTime());
    DocumentImpl doc2 = new DocumentImpl(this.uri2,this.txt2);
    doc2.setLastUseTime(System.nanoTime());  
    DocumentImpl doc3 = new DocumentImpl(this.uri3,this.txt3);
    doc3.setLastUseTime(System.nanoTime());  
    minHeap.insert(doc1);
    minHeap.insert(doc2);
    minHeap.insert(doc3);
    assertEquals(doc1.getDocumentTxt(), minHeap.remove().getDocumentTxt());
    }
    
    
    @Test
    public void simplePutandHeapremoval() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()),this.uri3, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt4.getBytes()),this.uri4, DocumentStore.DocumentFormat.TXT);
        store.setMaxDocumentCount(3);
        assertNull(store.getDocument(this.uri1),"Should be null");
    }
    @Test
    public void simplePutandGetLimitTest() throws IOException{
        DocumentStore store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()),this.uri3, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt4.getBytes()),this.uri4, DocumentStore.DocumentFormat.TXT);
        store.getDocument(this.uri1);
        store.setMaxDocumentCount(3);
        assertNull(store.getDocument(this.uri2));
    }
    @Test
    public void deleteDocumentTest()throws IOException{
        DocumentStore store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()),this.uri3, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt4.getBytes()),this.uri4, DocumentStore.DocumentFormat.TXT);
        store.deleteDocument(this.uri1);
        store.setMaxDocumentCount(2);
        assertNull(store.getDocument(this.uri2));
    }
    @Test
    public void undoHeapDocumentTest()throws IOException{
        DocumentStore store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()),this.uri3, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt4.getBytes()),this.uri4, DocumentStore.DocumentFormat.TXT);
        store.deleteDocument(this.uri3);
        store.deleteDocument(this.uri4);
        store.undo();
        store.undo();
        store.getDocument(this.uri1);
        store.setMaxDocumentCount(3);
        assertNull(store.getDocument(this.uri2));
        }
        @Test
        public void searchTest() throws IOException{
            DocumentStore store = new DocumentStoreImpl();
            store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
            store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
            store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()),this.uri3, DocumentStore.DocumentFormat.TXT);
            store.putDocument(new ByteArrayInputStream(this.txt4.getBytes()),this.uri4, DocumentStore.DocumentFormat.TXT);
            store.search("Computer");
            store.setMaxDocumentCount(3);
            assertNull(store.getDocument(this.uri2));
            assertEquals(0, store.search("A").size());
        }
        @Test
        public void deleteAllTest() throws IOException{
            DocumentStore store = new DocumentStoreImpl();
            store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
            store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
            store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()),this.uri3, DocumentStore.DocumentFormat.TXT);
            store.putDocument(new ByteArrayInputStream(this.txt4.getBytes()),this.uri4, DocumentStore.DocumentFormat.TXT);
            store.deleteAll("plain");
            store.undo();
            store.getDocument(this.uri4);
            store.setMaxDocumentCount(3);
            assertNull(store.getDocument(this.uri3));
        }
        @Test
        public void bytesTest() throws IOException{
            DocumentStore store = new DocumentStoreImpl();
            store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
            store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
            store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()),this.uri3, DocumentStore.DocumentFormat.TXT);
            store.putDocument(new ByteArrayInputStream(this.txt4.getBytes()),this.uri4, DocumentStore.DocumentFormat.TXT);
            store.deleteAll("plain");
            store.undo();
            store.getDocument(this.uri4);
            store.setMaxDocumentBytes(150);
            assertNull(store.getDocument(this.uri3));
        }
        @Test
        public void searchByPrefixTest() throws IOException{
            DocumentStore store = new DocumentStoreImpl();
            store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
            store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
            store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()),this.uri3, DocumentStore.DocumentFormat.TXT);
            store.putDocument(new ByteArrayInputStream(this.txt4.getBytes()),this.uri4, DocumentStore.DocumentFormat.TXT);
            store.searchByPrefix("p");
            store.setMaxDocumentBytes(150);
            assertNull(store.getDocument(this.uri3));
        }
        

    }
