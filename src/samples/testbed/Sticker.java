package samples.testbed;

public class Sticker extends Object {
     private String sticker;
     
     public Sticker(String s) {
    	 sticker=s;
     }
     public String getName() {
    	 return sticker;
     }
     
     public boolean equals(Object o) {
         return (o instanceof Sticker) && (((Sticker)o).getName()).equals(this.getName());
     }

     public int hashCode() {
         return sticker.hashCode();
     }
}
