import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CellIndexMethodSimulation extends JPanel {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 800;
    private final List<Particle> particles;
    private List<List<Integer>> neighbors;
    private final double L;
    private final double rc;
    private final int M;
    private final int N;
    private final boolean periodicBoundary;
    private int highlightedParticle = -1;

    public CellIndexMethodSimulation(double L, double rc, int N, boolean periodicBoundary) {
        this.L = L;
        this.rc = rc;
        this.N = N;
        this.periodicBoundary = periodicBoundary;
        this.particles = generateRandomParticles();
        this.M = calculateMaxM();
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
    }

    private List<Particle> generateRandomParticles() {
        List<Particle> particles = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < N; i++) {
            double x = random.nextDouble() * L;
            double y = random.nextDouble() * L;
            double radius = 0.25; // Fixed radius as per requirements
            particles.add(new Particle(i, x, y, radius));
        }
        return particles;
    }

    private int calculateMaxM() {
        double r = 0.25; // Fixed particle radius
        // The condition L/M > rc + 2r must be satisfied
        // We want the largest M that satisfies this condition
        // So, M < L / (rc + 2r)
        int maxM = (int) (L / (rc + 2 * r));
        // Ensure M is at least 1
        return Math.max(1, maxM);
    }

    public void runSimulation() {
        long startTime = System.nanoTime();
        neighbors = findNeighbors();
        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1e6; // Convert to milliseconds

        System.out.println("Execution time: " + duration + " ms");
        System.out.println("Number of cells (M): " + M);
        printNeighbors();
        repaint();
    }

    private List<List<Integer>> findNeighbors() {
        List<List<Integer>> neighbors = new ArrayList<>(N);
        for (int i = 0; i < N; i++) {
            neighbors.add(new ArrayList<>());
        }

        Cell[][] cells = new Cell[M][M];
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < M; j++) {
                cells[i][j] = new Cell();
            }
        }

        double cellSize = L / M;

        // Assign particles to cells
        for (Particle p : particles) {
            int cellX = (int) (p.x / cellSize);
            int cellY = (int) (p.y / cellSize);
            cells[cellX][cellY].particles.add(p);
        }

        // Find neighbors
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < M; j++) {
                for (Particle p1 : cells[i][j].particles) {
                    // Check same cell
                    for (Particle p2 : cells[i][j].particles) {
                        if (p1.id != p2.id && distance(p1, p2) < rc) {
                            neighbors.get(p1.id).add(p2.id);
                        }
                    }

                    // Check neighboring cells
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            if (dx == 0 && dy == 0) continue;
                            int nx = i + dx;
                            int ny = j + dy;

                            if (periodicBoundary) {
                                nx = (nx + M) % M;
                                ny = (ny + M) % M;
                            } else if (nx < 0 || nx >= M || ny < 0 || ny >= M) {
                                continue;
                            }

                            for (Particle p2 : cells[nx][ny].particles) {
                                if (distance(p1, p2) < rc) {
                                    neighbors.get(p1.id).add(p2.id);
                                }
                            }
                        }
                    }
                }
            }
        }

        return neighbors;
    }

    private double distance(Particle p1, Particle p2) {
        double dx = Math.abs(p1.x - p2.x);
        double dy = Math.abs(p1.y - p2.y);

        if (periodicBoundary) {
            dx = Math.min(dx, L - dx);
            dy = Math.min(dy, L - dy);
        }

        return Math.sqrt(dx * dx + dy * dy) - p1.radius - p2.radius;
    }

    private void printNeighbors() {
        for (int i = 0; i < neighbors.size(); i++) {
            System.out.print(i + ": ");
            for (int neighbor : neighbors.get(i)) {
                System.out.print(neighbor + " ");
            }
            System.out.println();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        double scale = Math.min(WIDTH, HEIGHT) / L;

        for (int i = 0; i < particles.size(); i++) {
            Particle p = particles.get(i);
            int x = (int) (p.x * scale);
            int y = (int) (p.y * scale);
            int diameter = (int) (p.radius * 2 * scale);

            if (i == highlightedParticle) {
                g2d.setColor(Color.RED);
            } else if (highlightedParticle != -1 && neighbors.get(highlightedParticle).contains(i)) {
                g2d.setColor(Color.GREEN);
            } else {
                g2d.setColor(Color.BLUE);
            }

            g2d.fillOval(x - diameter / 2, y - diameter / 2, diameter, diameter);
        }
    }

    public void setHighlightedParticle(int id) {
        this.highlightedParticle = id;
        repaint();
    }

    public static void main(String[] args) {
        double L = 20;
        double rc = 5;
        int N = 100; // Number of particles
        boolean periodicBoundary = true; // Can be changed to false for non-periodic boundary

        CellIndexMethodSimulation simulation = new CellIndexMethodSimulation(L, rc, N, periodicBoundary);
        simulation.runSimulation();

        JFrame frame = new JFrame("Cell Index Method Simulation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(simulation);
        frame.pack();
        frame.setVisible(true);

        // Highlight a random particle
        Random random = new Random();
        int highlightedParticle = random.nextInt(N);
        simulation.setHighlightedParticle(highlightedParticle);
    }
}
