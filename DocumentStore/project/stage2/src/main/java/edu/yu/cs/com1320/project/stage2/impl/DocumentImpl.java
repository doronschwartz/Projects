
package edu.yu.cs.com1320.project.stage2.impl;
import java.net.URI;
import java.util.*;
import edu.yu.cs.com1320.project.stage2.Document;
public class DocumentImpl implements Document{
    protected URI uri;
    protected String text;
    protected byte[] binaryData;
    
    public DocumentImpl(URI uri, String text){
        if(uri == null || text.length() == 0){
            throw new IllegalArgumentException();
        }
        this.uri = uri;
        this.text = text;
    }
    public DocumentImpl(URI uri, byte[] binaryData){
        if(uri == null || binaryData == null){
            throw new IllegalArgumentException();
        }
        this.uri = uri;
        this.binaryData = binaryData;
    }
    public String getDocumentTxt(){
        return text;
    }
    public byte[] getDocumentBinaryData(){
        return this.binaryData;
    }
    public URI getKey(){
        return this.uri;
    }
    @Override
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(binaryData);
        return result;
        }   
    @Override
    public boolean equals(Object d){
        return this.hashCode() == ((Document)(d)).hashCode();
    }    
}
