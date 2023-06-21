import java.util.HashMap;
import java.util.Scanner;
import java.awt.Image; 
import javax.swing.ImageIcon;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;

public class Element {
    public static HashMap<String, String> recipes = new HashMap<String, String>();

    public Image icon;
    public int x, y, w, h;
    public int kuuid = 0;
    public String id;

    public static void loadElements() throws FileNotFoundException {
        Scanner s = new Scanner(new File("src/recipes.txt"));
        int i = 0;
        while(s.hasNextLine()) {
            System.out.println("Line " + i++);
            String line = s.nextLine();
            if(line.equals("") || line.startsWith("#")) continue;
            line = line.trim().replaceAll(" ", "");
            String[] expressions = line.split("=");
            System.out.println(expressions[0] + " " + expressions[1]);
            recipes.put(expressions[0], expressions[1]);
        }
    }

    public static boolean canMixElements(Element a, Element b) {
        return recipes.containsKey(a.id + "," + b.id);
    }

    public static Element mixElements(Element a, Element b) {
        return new Element(recipes.get(a.id + "," + b.id));
    }

    public Element(String id) {
        System.out.println("'" + id + "'");
        this.icon = new ImageIcon(Element.class.getResource("img/" + id + ".png")).getImage();
        this.x = 100;
        this.y = 100;
        this.w = 64;
        this.h = 64;
        this.id = id;
    }
    public Rectangle getHitbox(int yoff) {
        return new Rectangle(this.x, this.y + yoff, this.w, this.h);
    }

    @Override public boolean equals(Object o) {
        if(o instanceof Element) {
            Element e = (Element) o;
            return e.id.equals(this.id);
        }
        return false;
    }

    @Override public Element clone() {
        Element e = new Element(this.id);
        e.x = this.x;
        e.y = this.y;
        kuuid *= 2;
        e.kuuid = kuuid + 1;
        return e;
    }

    @Override public String toString() {
        return "Element: " + this.id + " kuuid: " + kuuid + " at (" + this.x + ", " + this.y + ")";
    }
}
