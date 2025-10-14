package net.villagerzock;

import net.villagerzock.entity.Entity;

import java.util.Map;
import java.util.UUID;

public class Rect {
    private int x;
    private int y;
    private int width;
    private int height;

    // Konstruktoren
    public Rect(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Rect(Rect other) {
        this(other.x, other.y, other.width, other.height);
    }

    // ---- Getter/Setter ----
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    public int getLeft() { return x; }
    public int getRight() { return x + width; }
    public int getTop() { return y; }
    public int getBottom() { return y + height; }

    public int getCenterX() { return x + width / 2; }
    public int getCenterY() { return y + height / 2; }

    // ---- Methoden ----

    // Kopie
    public Rect copy() {
        return new Rect(this);
    }

    // Punkt-Kollision
    public boolean collidepoint(float px, float py) {
        return px >= getLeft() && px < getRight() &&
                py >= getTop() && py < getBottom();
    }

    // Rechteck-Kollision
    public boolean colliderect(Rect other) {
        return this.getRight() > other.getLeft() &&
                this.getLeft() < other.getRight() &&
                this.getBottom() > other.getTop() &&
                this.getTop() < other.getBottom();
    }

    // Enthält anderes Rechteck vollständig
    public boolean contains(Rect other) {
        return this.getLeft() <= other.getLeft() &&
                this.getRight() >= other.getRight() &&
                this.getTop() <= other.getTop() &&
                this.getBottom() >= other.getBottom();
    }

    // Verschieben (neues Objekt)
    public Rect move(int dx, int dy) {
        return new Rect(this.x + dx, this.y + dy, this.width, this.height);
    }

    // Verschieben in-place
    public void moveIp(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    // Clipping (Schnittmenge zweier Rects)
    public Rect clip(Rect other) {
        int newLeft = Math.max(this.getLeft(), other.getLeft());
        int newTop = Math.max(this.getTop(), other.getTop());
        int newRight = Math.min(this.getRight(), other.getRight());
        int newBottom = Math.min(this.getBottom(), other.getBottom());

        if (newRight <= newLeft || newBottom <= newTop) {
            return new Rect(0, 0, 0, 0);
        }
        return new Rect(newLeft, newTop, newRight - newLeft, newBottom - newTop);
    }

    // Union (einschließendes Rect)
    public Rect union(Rect other) {
        int newLeft = Math.min(this.getLeft(), other.getLeft());
        int newTop = Math.min(this.getTop(), other.getTop());
        int newRight = Math.max(this.getRight(), other.getRight());
        int newBottom = Math.max(this.getBottom(), other.getBottom());
        return new Rect(newLeft, newTop, newRight - newLeft, newBottom - newTop);
    }

    // In-place union
    public void unionIp(Rect other) {
        Rect u = this.union(other);
        this.x = u.x;
        this.y = u.y;
        this.width = u.width;
        this.height = u.height;
    }

    // Inflate (größer/kleiner machen, neues Objekt)
    public Rect inflate(int dx, int dy) {
        int newX = this.x - dx / 2;
        int newY = this.y - dy / 2;
        int newW = this.width + dx;
        int newH = this.height + dy;
        return new Rect(newX, newY, newW, newH);
    }

    // Inflate in-place
    public void inflateIp(int dx, int dy) {
        this.x -= dx / 2;
        this.y -= dy / 2;
        this.width += dx;
        this.height += dy;
    }

    // Skalieren mit Faktor
    public Rect scaleBy(double sx, double sy) {
        int newW = (int)Math.round(this.width * sx);
        int newH = (int)Math.round(this.height * sy);
        return new Rect(this.x, this.y, newW, newH);
    }

    // Skalieren in-place
    public void scaleByIp(double sx, double sy) {
        this.width = (int)Math.round(this.width * sx);
        this.height = (int)Math.round(this.height * sy);
    }

    // Normalisieren (falls negative Größen, auf positive korrigieren)
    public void normalize() {
        if (width < 0) {
            x += width;
            width = -width;
        }
        if (height < 0) {
            y += height;
            height = -height;
        }
    }

    // Clamp (in anderes Rect einpassen)
    public Rect clamp(Rect other) {
        Rect result = this.copy();
        result.clampIp(other);
        return result;
    }

    public void clampIp(Rect other) {
        if (this.width > other.width) {
            this.x = other.x + (other.width - this.width) / 2;
        } else {
            if (this.x < other.x) this.x = other.x;
            if (this.getRight() > other.getRight()) this.x = other.getRight() - this.width;
        }
        if (this.height > other.height) {
            this.y = other.y + (other.height - this.height) / 2;
        } else {
            if (this.y < other.y) this.y = other.y;
            if (this.getBottom() > other.getBottom()) this.y = other.getBottom() - this.height;
        }
    }

    // Update Werte
    public void update(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }

    @Override
    public String toString() {
        return "Rect(" + x + ", " + y + ", " + width + ", " + height + ")";
    }

    /**
     * Prüft, ob dieses Rechteck mit einer Entity oder einem Player kollidiert.
     * @return true wenn eine Kollision gefunden wird, sonst false
     */
    public boolean isCollidingWithEntity(Entity ignore) {
        // Zuerst alle Entities prüfen
        for (Map.Entry<UUID, Entity> entry : Main.entities.entrySet()) {
            if (entry.getValue().equals(ignore)) {
                continue;
            }
            Entity e = entry.getValue();
            if (e == null) continue;
            Rect other = e.getBounds();
            if (this.colliderect(other)) {
                return true;
            }
        }

        // Dann alle Spieler prüfen
        for (Map.Entry<UUID, Player> entry : Main.players.entrySet()) {
            Player p = entry.getValue();
            if (p == null) continue;
            Rect other = p.getBounds();
            if (this.colliderect(other)) {
                return true;
            }
        }

        return false;
    }
}
