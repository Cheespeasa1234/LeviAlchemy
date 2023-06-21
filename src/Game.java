import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class Game extends JPanel implements MouseListener, MouseMotionListener, KeyListener {

    public static final int PREF_W = 800;
    public static final int PREF_H = 600;

    public Game() {
        this.setFocusable(true);
        bg = new Color(170, 0, 210);
        this.setBackground(bg);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addKeyListener(this);

        elements = new ArrayList<Element>();
        discovered = new ArrayList<Element>();

        addToDiscovered(new Element("air"));
        addToDiscovered(new Element("earth"));
        addToDiscovered(new Element("fire"));
        addToDiscovered(new Element("water"));

        try {
            Element.loadElements();
        } catch (Exception e) {
            e.printStackTrace();
        }

        t.start();
    }

    public void addToDiscovered(Element e) {
        discovered.add(e);
        e.x = PREF_W - 100;
        e.y = discovered.size() * 64;
    }

    public void drawElement(Element e, Graphics2D g2, double rot, int rotx, int roty) {
        Color prev = g2.getColor();
        g2.setColor(Color.BLACK);
        g2.rotate(rot, rotx, roty);
        g2.drawImage(e.icon, e.x, e.y, e.w, e.h, this);
        g2.rotate(-rot, rotx, roty);
        g2.drawString(e.id, e.x, e.y);
        g2.setColor(prev);
    }

    private ArrayList<Element> elements;
    private ArrayList<Element> discovered;
    private int mousestartx, mousestarty;
    private int selstartx, selstarty;
    private boolean hoveringOverTrash = false;
    private float trashAnimationProgress = 0f;
    private float selectedItemShakeProgress = 0f;
    private int sidebarWidth = 100;
    private Color bg;
    private Element sel;
    private Timer t = new Timer(1000 / 60, e -> {
        repaint();

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
        g2.setBackground(bg);

        // discovered bar
        g2.setColor(bg.darker());
        g2.fillRect(PREF_W - sidebarWidth, 0, sidebarWidth, PREF_H);
        g2.setColor(Color.BLACK);
        discovered.forEach(element -> drawElement(element, g2, 0, 0, 0));

        // trash bar
        if (trashAnimationProgress != 0) {
            g2.setColor(Color.RED);
            double offset = Math.pow(2, trashAnimationProgress) / 2;
            g2.fillRect(PREF_W - (int) (sidebarWidth * offset), 0, sidebarWidth, PREF_H);
        }

        elements.forEach(element -> {
            if (element == sel) {
                double rot = Math.sin(selectedItemShakeProgress * Math.PI * 2) * 0.1;
                int rotx = element.x + element.w / 2;
                int roty = element.y + element.h / 2;
                drawElement(element, g2, rot, rotx, roty);
            } else {
                drawElement(element, g2, 0, 0, 0);
            }
        });
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (sel != null) {
            sel.x = selstartx - (mousestartx - e.getX());
            sel.y = selstarty - (mousestarty - e.getY());
            // if dragged over the trash
            if (sel.x + sel.w > PREF_W - 100) {
                hoveringOverTrash = true;
            } else {
                hoveringOverTrash = false;
                System.out.println();
            }
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
                if (el.getHitbox().contains(e.getPoint())) {
                    Element toUse = el.clone();
                    elements.add(toUse);
                    selectElement(toUse, e);
                    return;
                }
            }
        }

        for (int i = elements.size() - 1; i >= 0; i--) {
            Element el = elements.get(i);
            if (el.getHitbox().contains(e.getPoint())) {
                selectElement(el, e);
                // move to the top of the list
                elements.remove(el);
                elements.add(el);
                break;
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
                if (el.getHitbox().contains(e.getPoint()) && el != sel && Element.canMixElements(sel, el)) {

                    elements.remove(sel);
                    elements.remove(el);
                    Element res = Element.mixElements(sel, el);
                    res.x = el.x;
                    res.y = el.y;
                    elements.add(res);
                    if (!discovered.contains(res)) {
                        addToDiscovered(res.clone());
                    }

                    break;
                }
            }
        }
        sel = null;
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
