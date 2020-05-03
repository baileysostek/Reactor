package scripting;

import java.util.*;

public class Script implements Map {
    String name;
    //Convert this file to linear list of tokens.
    LinkedList<TokenCodePair> tokensInFile = new LinkedList<>();

    // Functions in this class
    HashMap<String, ScriptFunction> functions                    = new HashMap<>();
    HashMap<String, LinkedList<TokenCodePair>[]> nonlinearBlocks = new HashMap<>();
    // Variables in this class
    Scope scope;


    private boolean inComment = false;

    public Script(){
        scope = new Scope();
    }

    public Script(Scope scope){
        this.scope = scope;
    }

    public boolean hasFunction(String name){
        return functions.containsKey(name);
    }

    public LinkedList<TokenCodePair> getTokensInFile() {
        return tokensInFile;
    }

    public void setName(String fileName) {
        this.name = fileName;
    }

    public boolean getInComment(){
        return this.inComment;
    }

    public void setInComment(boolean inComment){
        this.inComment = inComment;
    }

    public Object getVar(String name){
        return scope.getVar(name);
    }

    public void addVar(String name, Object value){
        this.scope.addVar(name, value);
    }

    //Does not allow for recompile of nonlinearBlock
    public String addNonlearBlock(LinkedList<TokenCodePair> nonlinearBody){
        String id = nonlinearBlocks.size()+"";
        nonlinearBlocks.put(id, ScriptingEngine.getInstance().bodyToActions(nonlinearBody));
        return id;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public Object get(Object key) {
        return null;
    }

    @Override
    public Object put(Object key, Object value) {
        return null;
    }

    @Override
    public Object remove(Object key) {
        return null;
    }

    @Override
    public void putAll(Map m) {

    }

    @Override
    public void clear() {

    }

    @Override
    public Set keySet() {
        return null;
    }

    @Override
    public Collection values() {
        return null;
    }

    @Override
    public Set<Entry> entrySet() {
        return null;
    }
}