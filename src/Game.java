import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.SystemColor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class Game extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {

    public static int PREF_W = 800;
    public static int PREF_H = 600;

    private ArrayList<Element> elements;
    private ArrayList<Element> discovered;
    private int mousestartx, mousestarty;
    private int selstartx, selstarty;
    private boolean hoveringOverTrash = false;
    private float trashAnimationProgress = 0f;
    private float selectedItemShakeProgress = 0f;
    private int sidebarWidth = 100;
    private int sidebarPixelsScrolled = 0;
    private final int discoveredEntryHeightTotal = 75;
    private Color color1, color2, color3;
    private Font font1, font2;
    private Element sel;

    public Game() {
        this.setFocusable(true);
        this.setPreferredSize(new Dimension(PREF_W, PREF_H));

        color1 = new Color(50, 203, 255);
        color2 = new Color(0, 165, 224);
        color3 = new Color(222, 49, 121);
        font1 = new Font("Arial", Font.PLAIN, 14);
        font2 = new Font("Cascacida Mono", Font.BOLD, 40);

        this.setBackground(color1);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);

        // if resized
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                Game.PREF_W = evt.getComponent().getWidth();
                Game.PREF_H = evt.getComponent().getHeight();
                for (Element e : discovered) {
                    e.x = Game.PREF_W - sidebarWidth + 5;
                }
            }
        });

        elements = new ArrayList<Element>();
        discovered = new ArrayList<Element>();
        addToDiscovered(new Element("air"));
        addToDiscovered(new Element("earth"));
        addToDiscovered(new Element("fire"));
        addToDiscovered(new Element("water"));
        for (Element e : discovered) {
            e.x = Game.PREF_W - sidebarWidth + 5;
        }

        try {
            Element.loadElements();
        } catch (Exception e) {
            e.printStackTrace();
        }

        t.start();
    }

    public void addToDiscovered(Element e) {
        // insert it alphabetically
        int i = 0;
        for (Element d : discovered) {
            if (d.id.compareTo(e.id) > 0) {
                break;
            }
            i++;
        }
        discovered.add(i, e);

        e.x = PREF_W - 100;
        e.y = discovered.size() * (e.w + 10);
    }

    public void drawElement(Element e, int yoff, Graphics2D g2, double rot, int rotx, int roty) {
        Color prev = g2.getColor();
        g2.setColor(Color.BLACK);
        g2.rotate(rot, rotx, roty);
        g2.drawImage(e.icon, e.x, e.y + yoff, e.w, e.h, this);
        g2.rotate(-rot, rotx, roty + yoff);
        FontMetrics fm = g2.getFontMetrics();
        String txt = e.id.toUpperCase();
        int width = fm.stringWidth(txt);
        g2.drawString(txt, e.x + e.w / 2 - width / 2, e.y + e.h + 10 + yoff);
        g2.setColor(prev);
    }

    private Timer t = new Timer(1000 / 60, e -> {
        repaint();

        for (Element element : elements) {
            System.out.print(element.id + "(" + System.identityHashCode(element) + "), ");
        }
        System.out.println();

        // if hovering over the trash, increase the trash animation progress
        if (hoveringOverTrash) {
            System.out.println(trashAnimationProgress);
            trashAnimationProgress += 0.1;
            selectedItemShakeProgress += 0.1;
            selectedItemShakeProgress %= 1;
            trashAnimationProgress = Math.min(trashAnimationProgress, 1f);
        } else {
            trashAnimationProgress = 0;
            selectedItemShakeProgress = 0;
        }

        // round both anim progresses to 1 decimal place
        trashAnimationProgress = Math.round(trashAnimationProgress * 10) / 10f;
        selectedItemShakeProgress = Math.round(selectedItemShakeProgress * 10) / 10f;
    });

    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setFont(font1);
        g2.setBackground(color1);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // discovered bar
        g2.setColor(color2);
        g2.fillRect(PREF_W - sidebarWidth, 0, sidebarWidth, PREF_H);
        g2.setColor(Color.BLACK);
        discovered.forEach(element -> drawElement(element, sidebarPixelsScrolled, g2, 0, 0, 0));

        // trash bar
        if (trashAnimationProgress != 0) {
            g2.setColor(color3);
            double offset = Math.pow(2, trashAnimationProgress) / 2;
            g2.fillRect(PREF_W - (int) (sidebarWidth * offset), 0, sidebarWidth, PREF_H);
        }

        elements.forEach(element -> {
            if (element == sel) {
                double rot = Math.sin(selectedItemShakeProgress * Math.PI * 2) * 0.1;
                int rotx = element.x + element.w / 2;
                int roty = element.y + element.h / 2;
                drawElement(element, 0, g2, rot, rotx, roty);
            } else {
                drawElement(element, 0, g2, 0, 0, 0);
            }
        });

        g2.setFont(font2);
        g2.setColor(color2);
        FontMetrics fm = g2.getFontMetrics();
        String txt = "LeviAlchemy";
        g2.drawString(txt, PREF_W - sidebarWidth - fm.stringWidth(txt), PREF_H - 5);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (sel != null) {
            sel.x = selstartx - (mousestartx - e.getX());
            sel.y = selstarty - (mousestarty - e.getY());
            // if dragged over the trash
            if (sel.x + sel.w > PREF_W - sidebarWidth) {
                hoveringOverTrash = true;
            } else {
                hoveringOverTrash = false;
            }
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        sidebarPixelsScrolled += e.getWheelRotation() * -10;
        System.out.println("Scrolled:" + sidebarPixelsScrolled);
    }

    public void selectElement(Element element, MouseEvent event) {
        sel = element;
        selstartx = element.x;
        selstarty = element.y;
        mousestartx = event.getX();
        mousestarty = event.getY();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // if pressed to the right of the sidebar
        if (e.getX() > PREF_W - sidebarWidth) {
            System.out.println("selected from sidebar");
            // select the element that was clicked on
            for (int i = 0; i < discovered.size(); i++) {
                Element el = discovered.get(i);
                if (el.getHitbox(sidebarPixelsScrolled).contains(e.getPoint())) {
                    Element toUse = el.clone();
                    toUse.y += sidebarPixelsScrolled;
                    elements.add(toUse);
                    selectElement(toUse, e);
                    return;
                }
            }
        }

        for (int i = elements.size() - 1; i >= 0; i--) {
            Element el = elements.get(i);
            if (el.getHitbox(0).contains(e.getPoint())) {
                selectElement(el, e);
                // move to the top of the list
                elements.remove(i);
                elements.add(el);
                return;
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // if released over the trash
        if (hoveringOverTrash) {
            elements.remove(sel);
            sel = null;
            hoveringOverTrash = false;
        }
        // if released on top of another piece
        if (sel != null) {
            for (int i = elements.size() - 1; i >= 0; i--) {
                Element el = elements.get(i);
                if (el.getHitbox(0).contains(e.getPoint()) && el != sel && Element.canMixElements(sel, el)) {

                    elements.remove(sel);
                    elements.remove(el);
                    Element res = Element.mixElements(sel, el);
                    res.x = el.x;
                    res.y = el.y;
                    elements.add(res);
                    if (!discovered.contains(res)) {
                        addToDiscovered(res.clone());
                    }

                    return;
                }
            }
            sel = null;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    /* METHODS FOR CREATING JFRAME AND JPANEL */

    public Dimension getPreferredSize() {
        return new Dimension(PREF_W, PREF_H);
    }

    public static void createAndShowGUI() {
        JFrame frame = new JFrame("You're Mother");
        JPanel gamePanel = new Game();

        frame.getContentPane().add(gamePanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    // ¯\_(ツ)_/¯ welp, you heard the man!
    public String shouldICreditCopilotAndWhy() {
        return "yes, because he is a good boi and he helped me with the code.";
    }

}
