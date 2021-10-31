package edu.yu.cs.com1320.project.stage5.impl;



import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.yu.cs.com1320.project.stage5.Document;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;



public class DocumentPersistanceManagerTests {
    private URI textUri;
    private String textString;

    private URI binaryUri;
    private byte[] binaryData;

    @BeforeEach
    public void setUp() throws Exception {
        this.textUri = new URI("http://edu.yu.cs/com1320/txt");
        this.textString = "This is text content. Lots of it.";

        this.binaryUri = new URI("http://edu.yu.cs/com1320/binary");
        this.binaryData = "This is a PDF, brought to you by Adobe.".getBytes();

    }
    @Test
    public void TextDocumentTest() throws IOException, URISyntaxException{
        DocumentPersistenceManager d = new DocumentPersistenceManager(null);
        Document doc = new DocumentImpl(new URI("abc://www.yu.edu/documents/doc1"),"Text for doc1");
        d.serialize(new URI("abc://www.yu.edu/documents/doc1"), doc);  
        Document returned = d.deserialize(new URI("abc://www.yu.edu/documents/doc1"));
        assertEquals(returned.getDocumentTxt(),"Text for doc1");
    }
    @Test
    public void BinaryDocumentTest() throws IOException, URISyntaxException {
        DocumentPersistenceManager d = new DocumentPersistenceManager(null);
        byte[] binaryData = "This is a PDF, brought to you by Adobe.".getBytes();
        Document doc2 = new DocumentImpl(new URI("www.yu.edu/documents/doc2"),binaryData);
        d.serialize(new URI("www.yu.edu/documents/doc2"),doc2);
        Document returnedtwo = d.deserialize(new URI("abc://www.yu.edu/documents/doc2"));
        assertTrue(Arrays.equals(returnedtwo.getDocumentBinaryData(),"This is a PDF, brought to you by Adobe.".getBytes()));
        assertEquals(doc2,returnedtwo);
    }
}