package find_the_animal;

public class RuleManager {
    
    private final int max_memory_size;
    
    private final java.util.ArrayList<Integer> memory;
    
    private final int nbData;
    private Integer current;
    
    public RuleManager(int nbData) {
        
        if(nbData<3) {
            throw new RuntimeException("RuleManager: nbData must be >= 3");
        }
        
        this.nbData     = nbData;
        max_memory_size = (nbData/2)+1;
        current         = null;
        memory          = new java.util.ArrayList<>();
    }
    
    public void setCurrent() {
        do {
            current = (int)(Math.random()*nbData);
        } while(memory.contains(current)) ;
            
        memory.add(current);
        if(memory.size()>max_memory_size) {
            memory.remove(0);
        }
    }
        
    
    public Integer getCurrent() {
        return current;
    }
    
    @Override
    public String toString(){
        String state="There are "+nbData+" values, the memory maximum size is "+max_memory_size+" and the current one is "+current+"\n";
        if(memory.isEmpty()) {
            state+="No values have already saved in memory";
        }else {
            state+="Already saved values:";
            for (java.lang.Integer value : memory) {
                state+=" "+value;
            }
        }
        return state;    
    }
    
}
