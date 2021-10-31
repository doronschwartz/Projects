package edu.yu.cs.com1320.project.stage5.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import javax.xml.bind.DatatypeConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;

/**
 * created by the document store and given to the BTree via a call to BTree.setPersistenceManager
 */
public class DocumentPersistenceManager implements PersistenceManager<URI, Document>{
    private File baseDir;
    class DocSerializer implements JsonSerializer<Document>{

        @Override
        public JsonElement serialize(Document src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject(); 
            if(src.getDocumentTxt() != null){
                String text = src.getDocumentTxt();
                object.addProperty("text", text);
            }
            else{
                
                String base64Encoded = DatatypeConverter.printBase64Binary(src.getDocumentBinaryData());
                JsonArray jsonArray1 = new Gson().toJsonTree(src.getDocumentBinaryData()).getAsJsonArray();
                object.add("binaryData",jsonArray1);
            }
            Gson mapbuilder = new Gson();
            JsonObject json = mapbuilder.toJsonTree(src.getWordMap()).getAsJsonObject();
            
            object.add("count", json);
            
            object.addProperty("uri",src.getKey().toString());
            return object;
        }

    }
  
   
    
    class DocDeseriazler implements JsonDeserializer<Document>{

        @Override
        public Document deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
                        Gson gson = new Gson();
                        Document document = gson.fromJson(json,typeOfT);
                        Type type = new TypeToken<HashMap<String,Integer>>(){}.getType();
                        HashMap<String,Integer> wordMap = gson.fromJson(json.getAsJsonObject().get("count"), type);
                        document.setWordMap(wordMap);
                        return document;
            }
        }
              
    public DocumentPersistenceManager(File baseDir){
        this.baseDir = baseDir;
    }

    @Override
    public void serialize(URI uri, Document val) throws IOException {
        GsonBuilder gson = new GsonBuilder();
        gson.registerTypeAdapter(val.getClass(), new DocSerializer());
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
        complete.getParentFile().mkdirs(); 
        complete.createNewFile();
        FileWriter writer = new FileWriter(complete);
        
        gson.create().toJson(val,writer);
        writer.close();
    }

    @Override
    public Document deserialize(URI uri) throws IOException {
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
        Path path = Paths.get(new File(base,rawScheme).getAbsolutePath());
        if(!(Files.exists(path))){
            return null;
        }
        Reader reader = Files.newBufferedReader(path);
        
        Gson gson = new GsonBuilder().registerTypeAdapter(DocumentImpl.class, new DocDeseriazler()).create();
        Document document = gson.fromJson(reader, DocumentImpl.class);
        reader.close();
        this.delete(uri);
        return document;
    }

    @Override
    public boolean delete(URI uri) throws IOException {
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
        Path path = Paths.get(new File(base,rawScheme).getAbsolutePath());
        if(!(Files.exists(path))){
            return false;
        }
        Files.delete(path);
        return true;
    }
    
   
    public static void main(String[] args) throws IOException, URISyntaxException {
        DocumentPersistenceManager d = new DocumentPersistenceManager(null);
        Document doc = new DocumentImpl(new URI("abc://www.yu.edu/documents/doc1"),"Text for doc1");
        d.serialize(new URI("abc://www.yu.edu/documents/doc1"), doc);  
        Document returned = d.deserialize(new URI("abc://www.yu.edu/documents/doc1"));
        System.out.println(returned.getDocumentTxt().equals("Text for doc1"));
        byte[] binaryData = "This is a PDF, brought to you by Adobe.".getBytes();
        Document doc2 = new DocumentImpl(new URI("www.yu.edu/documents/doc2"),binaryData);
        d.serialize(new URI("www.yu.edu/documents/doc2"),doc2);
        Document returnedtwo = d.deserialize(new URI("abc://www.yu.edu/documents/doc2"));
        System.out.println(returnedtwo.getDocumentBinaryData().equals("This is a PDF, brought to you by Adobe.".getBytes()));




       

    }


   
}