import java.util.*;

public class CellIndexMethod {

    private List<Particle> particles;
    private final Map<Integer, Set<Particle>> neighbors;
    final double L;
    final double rc;
    private int M;
    final int N;
    private Cell[][] cells;
    private double maxParticleRadius;

    CellIndexMethod(List<Particle> particles, double L, int M, int N, double rc) {
        this.particles = particles;
        this.L = L;
        this.M = M;
        this.N = N;
        this.rc = rc;
        this.neighbors = new HashMap<>(N);
        for (int i = 0; i < N; i++) {
            this.neighbors.put(i, new HashSet<>());
        }
        this.maxParticleRadius = findMaxRadius(particles);
    }

    CellIndexMethod(double L, int N, double rc) {
        this.L = L;
        this.N = N;
        this.rc = rc;
        this.neighbors = new HashMap<>(N);
        for (int i = 0; i < N; i++) {
            this.neighbors.put(i, new HashSet<>());
        }
        this.generateIdealM();
        this.maxParticleRadius = 0;
    }

    CellIndexMethod(double L, int N, double rc, int M) {
        this.L = L;
        this.N = N;
        this.rc = rc;
        this.M = M;
        this.neighbors = new HashMap<>(N);
        for (int i = 0; i < N; i++) {
            this.neighbors.put(i, new HashSet<>());
        }
        this.maxParticleRadius = 0;
    }

    CellIndexMethod generateRandomParticles() {
        List<Particle> toSet = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < N; i++) {
            double x = random.nextDouble() * L;
            double y = random.nextDouble() * L;
            toSet.add(new Particle(i, x, y));
        }
        particles = toSet;
        return this;
    }

    private double findMaxRadius(List<Particle> particles) {
        return particles.stream()
                .mapToDouble(p -> p.radius)
                .max()
                .orElse(0);
    }

    CellIndexMethod generateRandomParticles(double maxRadius) {
        List<Particle> toSet = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < N; i++) {
            double x = random.nextDouble() * L;
            double y = random.nextDouble() * L;
//            double radius = random.nextDouble() * maxRadius;
            toSet.add(new Particle(i, x, y, maxRadius));
        }
        particles = toSet;
        this.maxParticleRadius = maxRadius;
        if (!checkMValue()){
            generateIdealM();
        }
        return this;
    }

    private boolean checkMValue() {
        return (L / M) >= (rc + 2 * maxParticleRadius);
    }

    void generateIdealM() {
        int maxM = (int) (L / (rc + 2 * maxParticleRadius));
        // Ensure M is at least 1
        M = Math.max(1, maxM);
    }

    long runSimulation() {
        long startTime = System.nanoTime();  // Start time in nanoseconds

        if (!checkMValue()) {
            throw new RuntimeException("Invalid M value. The condition L/M >= rc must be satisfied");
        }
        createCells();
        assignParticlesToCells();
        calculateNeighbors();

        long endTime = System.nanoTime();  // End time in nanoseconds

        long duration = endTime - startTime;  // Calculate the duration
        double milliseconds = (double) duration /1_000_000;
//        System.out.printf("N: %d, L: %f, M: %d, rc: %f\tDuration(ms): %f\n", N, L, M, rc, milliseconds);

        return duration;  // Return the duration in nanoseconds
    }



    private void createCells() {
        cells = new Cell[M][M];
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < M; j++) {
                cells[i][j] = new Cell();
            }
        }
    }

    private void assignParticlesToCells() {
        double cellSize = L / M;
        for (Particle p : particles) {
            int cellX = (int) (p.x / cellSize);
            int cellY = (int) (p.y / cellSize);
            cells[cellX][cellY].particles.add(p);
        }
    }

    private void calculateNeighbors() {
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < M; j++) {
                for (Particle p1 : cells[i][j].particles) {
                    // Check neighbors in current cell
                    checkNeighborsInCell(p1, i, j);

                    // Check neighbors in adjacent cells (top-right L shape and bottom-right)
                    checkNeighborsInCell(p1, i, (j + 1) % M); // Top
                    checkNeighborsInCell(p1, (i + 1) % M, (j + 1) % M); // Top-right
                    checkNeighborsInCell(p1, (i + 1) % M, j); // Right
                    checkNeighborsInCell(p1, (i + 1) % M, (j - 1 + M) % M); // Bottom-right
                }
            }
        }
    }

    private void checkNeighborsInCell(Particle p1, int cellX, int cellY) {
        for (Particle p2 : cells[cellX][cellY].particles) {
            if (p1.id != p2.id) {
                double dx = Math.abs(p1.x - p2.x);
                double dy = Math.abs(p1.y - p2.y);

                // Apply periodic boundary conditions
                dx = Math.min(dx, L - dx);
                dy = Math.min(dy, L - dy);

                double distance = Math.sqrt(dx * dx + dy * dy);
                if (distance <= rc + p1.radius + p2.radius){
                    neighbors.get(p1.id).add(p2);
                    neighbors.get(p2.id).add(p1);
                }
            }
        }
    }

    long runSimulationWithWalls() {
        long startTime = System.nanoTime();

        if (!checkMValue()) {
            throw new RuntimeException("Invalid M value. The condition L/M >= rc must be satisfied");
        }
        createCells();
        assignParticlesToCells();
        calculateNeighborsWithWalls();

        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        double milliseconds = (double) duration / 1_000_000;
        // System.out.printf("N: %d, L: %f, M: %d, rc: %f\tDuration(ms): %f\n", N, L, M, rc, milliseconds);

        return duration;
    }

    private void calculateNeighborsWithWalls() {
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < M; j++) {
                for (Particle p1 : cells[i][j].particles) {
                    // Check neighbors in current cell
                    checkNeighborsInCellWithWalls(p1, i, j);

                    // Check neighbors in adjacent cells (top-right L shape and bottom-right)
                    if (j < M - 1) checkNeighborsInCellWithWalls(p1, i, j + 1); // Top
                    if (i < M - 1 && j < M - 1) checkNeighborsInCellWithWalls(p1, i + 1, j + 1); // Top-right
                    if (i < M - 1) checkNeighborsInCellWithWalls(p1, i + 1, j); // Right
                    if (i < M - 1 && j > 0) checkNeighborsInCellWithWalls(p1, i + 1, j - 1); // Bottom-right
                }
            }
        }
    }

    private void checkNeighborsInCellWithWalls(Particle p1, int cellX, int cellY) {
        for (Particle p2 : cells[cellX][cellY].particles) {
            if (p1.id != p2.id) {
                double dx = Math.abs(p1.x - p2.x);
                double dy = Math.abs(p1.y - p2.y);

                // No periodic boundary conditions applied
                double distance = Math.sqrt(dx * dx + dy * dy);
                if (distance <= rc + p1.radius + p2.radius) {
                    neighbors.get(p1.id).add(p2);
                    neighbors.get(p2.id).add(p1);
                }
            }
        }
    }

    public Map<Integer, Set<Particle>> getNeighbors() {
        return neighbors;
    }

    public List<Particle> getParticles() {
        return particles;
    }
}