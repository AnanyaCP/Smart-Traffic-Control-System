package Project;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

/**
 * THE CONTROLLER & VIEW: TrafficApp
 * Implements a 3-lane two-way intersection.
 * Lane 0: Incoming (Normal - Stops at Red)
 * Lane 1: Outgoing (Opposite Direction - Never Stops)
 * Lane 2: Emergency Middle (Stops at Red, priority during override)
 */
public class TrafficApp extends JFrame {
    private TrafficEngine engine = new TrafficEngine();
    private JPanel roadPanel;

    public TrafficApp() {
        setTitle("AI Traffic Command - 3 Lane Two-Way System");
        setSize(1000, 900);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Drawing surface for the simulation
        roadPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawIntersection((Graphics2D) g);
            }
        };
        roadPanel.setBackground(new Color(15, 20, 15));

        // Emergency Control Buttons
        JPanel btns = new JPanel(new GridLayout(1, 4, 10, 0));
        btns.setBackground(new Color(30, 30, 35));
        btns.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        String[] roads = {"North", "East", "South", "West"};
        for (int i = 0; i < 4; i++) {
            final int id = i;
            JButton b = new JButton("🚑 Emergency: " + roads[id]);
            b.setBackground(new Color(180, 0, 0));
            b.setForeground(Color.WHITE);
            b.setFocusPainted(false);
            b.addActionListener(e -> { 
                engine.addEmergency(id); 
                spawn(id, true, false); // Spawn ambulance in Lane 2
            });
            btns.add(b);
        }

        add(roadPanel, BorderLayout.CENTER);
        add(btns, BorderLayout.SOUTH);

        // Repaint UI on engine updates
        engine.setOnUpdate(() -> roadPanel.repaint());
        
        // Start system threads
        engine.startLogicThread();
        new Thread(this::animationLoop).start();
    }

    private void drawIntersection(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int mx = roadPanel.getWidth()/2, my = roadPanel.getHeight()/2, rw = 180;

        // Draw Roads
        g.setColor(new Color(40, 40, 45));
        g.fillRect(mx - rw/2, 0, rw, roadPanel.getHeight());
        g.fillRect(0, my - rw/2, roadPanel.getWidth(), rw);

        // 3-Lane Divider Markings
        g.setColor(new Color(255, 255, 255, 60));
        g.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{10}, 0));
        g.drawLine(mx - 30, 0, mx - 30, roadPanel.getHeight());
        g.drawLine(mx + 30, 0, mx + 30, roadPanel.getHeight());
        g.drawLine(0, my - 30, roadPanel.getWidth(), my - 30);
        g.drawLine(0, my + 30, roadPanel.getWidth(), my + 30);

        // Render Traffic Signals
        for (int i = 0; i < 4; i++) {
            boolean active = (engine.activeRoad == i);
            String st = active ? engine.signalState : "RED";
            int sx = (i==0||i==3) ? mx-160 : mx+125;
            int sy = (i==0||i==1) ? my-160 : my+125;
            
            g.setColor(new Color(10, 10, 15));
            g.fillRoundRect(sx, sy, 40, 80, 10, 10);
            
            Color c = st.equals("GREEN") ? Color.GREEN : st.equals("ORANGE") ? Color.ORANGE : Color.RED;
            g.setColor(c); 
            g.fillOval(sx + 10, sy + 10, 20, 20);
            
            g.setColor(Color.WHITE);
            g.setFont(new Font("Monospaced", Font.BOLD, 18));
            g.drawString(active ? String.format("%02d", engine.timer) : "--", sx+10, sy+65);
        }

        // Render Vehicles
        synchronized(engine.vehicles) {
            for (Vehicle v : engine.vehicles) {
                g.setColor(v.color);
                g.fillRoundRect(v.x, v.y, v.getW(), v.getH(), 8, 8);
                if (v.isAmbulance) {
                    g.setColor(System.currentTimeMillis() % 400 > 200 ? Color.RED : Color.BLUE);
                    g.fillOval(v.x + v.getW()/2 - 5, v.y + v.getH()/2 - 5, 10, 10);
                }
            }
        }
    }

    private void animationLoop() {
        Random r = new Random();
        while (true) {
            if (r.nextInt(100) < 4) {
                spawn(r.nextInt(4), false, r.nextBoolean());
            }
            
            synchronized(engine.vehicles) {
                int mx = roadPanel.getWidth()/2, my = roadPanel.getHeight()/2;
                for (int i = engine.vehicles.size()-1; i >= 0; i--) {
                    Vehicle v = engine.vehicles.get(i);
                    boolean isGreen = (engine.activeRoad == v.roadId && engine.signalState.equals("GREEN"));
                    boolean blocked = false;

                    // Emergency Mode: Only ambulances move
                    if (engine.emergencyOverride && !v.isAmbulance) {
                        blocked = true;
                    } 
                    
                    // Anti-Overlap checking: Strict Lane and Road matching
                    // Safety Buffer is set to 110 pixels to prevent crowding
                    for (Vehicle o : engine.vehicles) {
                        if (v == o || v.roadId != o.roadId || v.lane != o.lane) continue;
                        
                        int dist = 0;
                        if (v.roadId == 0) dist = (v.lane == 1) ? v.y - o.y : o.y - v.y;
                        else if (v.roadId == 1) dist = (v.lane == 1) ? o.x - v.x : v.x - o.x;
                        else if (v.roadId == 2) dist = (v.lane == 1) ? o.y - v.y : v.y - o.y;
                        else if (v.roadId == 3) dist = (v.lane == 1) ? v.x - o.x : o.x - v.x;

                        if (dist > 0 && dist < 110) { 
                            blocked = true; 
                            break; 
                        }
                    }

                    // Stop logic for Incoming (Lane 0) and Emergency (Lane 2)
                    // REMOVED hard-coordinate snapping to prevent overlaps on East/South roads
                    if (!blocked && !isGreen && (v.lane == 0 || v.lane == 2)) { 
                        if (v.roadId == 0 && v.y >= my-250 && v.y <= my-145) {
                            blocked = true;
                        }
                        else if (v.roadId == 1 && v.x <= mx+250 && v.x >= mx+145) {
                            blocked = true;
                        }
                        else if (v.roadId == 2 && v.y <= my+250 && v.y >= my+145) {
                            blocked = true;
                        }
                        else if (v.roadId == 3 && v.x >= mx-250 && v.x <= mx-145) {
                            blocked = true;
                        }
                    }

                    if (!blocked) v.move();
                    
                    // Cleanup
                    if (v.x < -200 || v.x > roadPanel.getWidth() + 200 || v.y < -200 || v.y > roadPanel.getHeight() + 200) {
                        engine.vehicles.remove(i);
                    }
                }
            }
            roadPanel.repaint();
            try { Thread.sleep(30); } catch (Exception e) {}
        }
    }

    private void spawn(int road, boolean amb, boolean outgoing) {
        int mx = roadPanel.getWidth()/2, my = roadPanel.getHeight()/2;
        if (mx == 0 || my == 0) return;

        int lane = amb ? 2 : (outgoing ? 1 : 0); 
        int x=0, y=0;
        int off = (lane == 0) ? -65 : (lane == 2 ? 0 : 65);
        
        // Dynamic spawn points based on lane direction
        if(road==0){ x=mx+off-18; y=outgoing?roadPanel.getHeight()+100:-100; } 
        else if(road==1){ x=outgoing?-100:roadPanel.getWidth()+100; y=my+off-18; }
        else if(road==2){ x=mx-off-18; y=outgoing?-100:roadPanel.getHeight()+100; } 
        else if(road==3){ x=outgoing?roadPanel.getWidth()+100:-100; y=my-off-18; }
        
        synchronized(engine.vehicles) {
            for(Vehicle v : engine.vehicles) {
                // Increased spawn collision buffer to 140 to ensure safe entry
                if(v.lane == lane && Math.abs(v.x-x) < 140 && Math.abs(v.y-y) < 140) return;
            }
            engine.vehicles.add(new Vehicle(x, y, amb?8:4, road, lane, amb?Color.WHITE:new Color(100,160,255), amb));
        }
    }

    public static void main(String[] args) { 
        SwingUtilities.invokeLater(()->new TrafficApp().setVisible(true)); 
    }
}