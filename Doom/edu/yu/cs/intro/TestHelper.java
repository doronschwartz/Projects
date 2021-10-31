package edu.yu.cs.intro;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class TestHelper {

    private String errorHolder;
    private String paramsError;
    private int charDifLimit;

    public String getErrorMessages(){
        return this.errorHolder;
    }
    public int getCharDifLimit() {
        return charDifLimit;
    }

    public void setCharDifLimit(int charDifLimit) {
        this.charDifLimit = charDifLimit;
    }

    public boolean hasConstructor(Class clazz, Class[] params){
        try{
            clazz.getConstructor(params);
            return true;
        }catch(NoSuchMethodException e){
            return false;
        }
    }

    public boolean methodSignature(String methodName, Class returnType, Class[] expectedParameters, Class clazz, boolean shouldbeStatic) {
        boolean isValid = true;
        boolean foundMethod = false;
        this.errorHolder = "";
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                //check parameters - only same method if same signature!
                if (parameterTypes(expectedParameters, method.getParameterTypes())) {
                    foundMethod = true;
                    int mods = method.getModifiers();
                    //check public
                    if (!Modifier.isPublic(mods)) {
                        this.errorHolder += "The " + methodName + " method is not public.\n";
                        isValid = false;
                    }
                    //check static
                    if (shouldbeStatic && !Modifier.isStatic(mods)) {
                        this.errorHolder += "The " + methodName + " method is not static.\n";
                        isValid = false;
                    }
                    //check return type
                    if (!method.getReturnType().equals(returnType)) {
                        this.errorHolder += "The " + methodName + " method does not have \"" + returnType.getName() + "\" as its return type.\n";
                        isValid = false;
                    }
                    //print out errors
                    if (!this.errorHolder.isEmpty()) {
                        System.out.println(this.errorHolder);
                    }
                }
            }
        }
        if (isValid && foundMethod) {
            return true;
        }
        else {
            this.errorHolder += this.paramsError;
            return false;
        }
    }

    public boolean parameterTypes(Class[] expectedParameters, Class[] actual) {
        this.paramsError = "";
        if (expectedParameters.length != actual.length) {
            this.paramsError = "Parameters do not match required parameter types & order. Expected: " + getTypeSequence(expectedParameters) + ". Actual: " + getTypeSequence(actual);
            return false;
        }
        //compare arrays
        for (int i = 0; i < expectedParameters.length; i++) {
            if (!expectedParameters[i].equals(actual[i])) {
                this.paramsError = "Parameters do not match required parameter types & order. Expected: " + getTypeSequence(expectedParameters) + ". Actual: " + getTypeSequence(actual);
                return false;
            }
        }
        return true;
    }
    private String getTypeSequence(Class[] clazzes){
        String types = "";
        for(int i = 0; i < clazzes.length; i++){
            if(i > 0){
                types += ", ";
            }
            types += clazzes[i].getCanonicalName();
        }
        return types;
    }

    private PrintStream tempOut;
    private ByteArrayOutputStream myout;
    private PrintStream tempErr;
    private ByteArrayOutputStream myerr;

    public String getExceptionOutput(Throwable e) {
        String output = e.getMessage() + "\n";
        this.beginOutputRedirect();
        e.printStackTrace();
        return output + this.endOutputRedirect();
    }

    public String getStackTraceAsString(Throwable e){
        this.beginOutputRedirect();
        e.printStackTrace();
        return this.endOutputRedirect();
    }

    public void beginOutputRedirect() {
        tempOut = System.out;
        tempErr = System.err;
        myout = new ByteArrayOutputStream();
        myerr = new ByteArrayOutputStream();
        try {
            tempOut.flush();
            tempErr.flush();
        }
        catch (Exception e) {
        }
        System.setOut(new PrintStream(myout));
        System.setErr(new PrintStream(myerr));
    }

    public String endOutputRedirect() {
        System.setOut(tempOut);
        System.setErr(tempErr);
        try {
            myout.flush();
            myerr.flush();
        }
        catch (Exception e) {
        }
        return myout.toString() + myerr.toString();
    }

    /**
     * @param expected
     * @param actual
     * @return null if they match, error message if they don't
     */
    public String compareOutput(String expected, String actual, String input, boolean dropTrailingInconsistentNewline) {
        if(dropTrailingInconsistentNewline && actual.endsWith("\n") && !expected.endsWith("\n")){
            actual = actual.substring(0,actual.length()-1);
        }else if(dropTrailingInconsistentNewline && !actual.endsWith("\n") && expected.endsWith("\n")){
            expected = expected.substring(0,expected.length()-1);
        }
        //System.out.println("comparing outputs:");
        if (actual.equals(expected)) {
            return null;
        }
        System.out.println("expected.length() = " + expected.length());
        System.out.println("actual.length() = " + actual.length());
        int difCount = 0;
        for(int i = 0; i < expected.length() && i < actual.length(); i++){
            if(expected.charAt(i) != actual.charAt(i) && difCount < getCharDifLimit()){
                System.out.println("the " + i + " char is not the same; expected.charAt(i) = " + expected.charAt(i) + "|| and the actual.charAt(i) is  " + actual.charAt(i));
                difCount++;
            }
        }

        return "***INPUT***:\n" + input + "\n***EXPECTED OUTPUT***:\n" + expected + "\n***YOUR OUTPUT:***\n" + actual;
    }

    public String runMain(Class clazz, String[] args) {
        this.beginOutputRedirect();
        try {
            Method main = clazz.getDeclaredMethod("main", String[].class);
            main.invoke(null,(Object)args);
        }catch(Exception e){
            e.printStackTrace();
        }
        return this.endOutputRedirect();
    }

    public void runMethod(Class clazz, Object obj, String methodName, Object... args) throws Exception{
        try {
            Method m = null;
            if(args == null){
                m = clazz.getDeclaredMethod(methodName);
            }else{
                m = clazz.getDeclaredMethod(methodName,getTypes(args));
            }
            m.invoke(obj,args);
        }catch(Exception e){
            throw e;
        }
    }

    private static Class[] getTypes(Object... args){
        Class[] types = new Class[args.length];
        for(int i = 0; i < types.length; i++){
            types[i] = args[i].getClass();
        }
        return types;
    }

    public boolean implementsInterface(Class clazz, Class iface){
        Class[] ifaces = clazz.getInterfaces();
        for(Class current : ifaces){
            if(current.equals(iface)){
                return true;
            }
        }
        return false;
    }


}
