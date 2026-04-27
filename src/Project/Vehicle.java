package Project;

import java.awt.Color;

public class Vehicle {
    public int x, y, speed, roadId, lane;
    public Color color;
    public boolean isAmbulance;

    public Vehicle(int x, int y, int speed, int roadId, int lane, Color color, boolean isAmbulance) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.roadId = roadId;
        this.lane = lane; // 0: Normal In, 1: Normal Out, 2: Emergency Middle
        this.color = color;
        this.isAmbulance = isAmbulance;
    }

    public int getW() { return (roadId % 2 == 0) ? 36 : 54; }
    public int getH() { return (roadId % 2 == 0) ? 54 : 36; }

    public void move() {
        if (roadId == 0) y += speed;      // North to South
        else if (roadId == 1) x -= speed; // East to West
        else if (roadId == 2) y -= speed; // South to North
        else if (roadId == 3) x += speed; // West to East
    }
}
