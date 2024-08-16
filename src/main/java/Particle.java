public class Particle {
    double x, y, radius;
    int id;

    public Particle(int id, double x, double y, double radius) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    public Particle(int id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.radius = 0;
    }
}
