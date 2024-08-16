import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class runner {
    public static void main(String[] args) {

        int iterations = 50;
        int minN = 500;
        double minL = 500;
        double minRc = 50;

//        compareBruteForce(iterations, minN, minL, minRc);

        CellIndexMethod md = new CellIndexMethod(minL, minN, minRc)
                .generateRandomParticles();

//        md.runSimulation();

        md.runSimulationWithWalls();

        createNeighborsFile(md);
        createParticlesFile(md);

    }

    private static void compareBruteForce(int iterations, int minN, double minL, double minRc){
        String fileName = "cell_index_method_results.csv";
        FileWriter csvWriter = null;
        try {
            csvWriter = new FileWriter(fileName);
            csvWriter.append("Configuration,N,L,rc,RuntimeDiff_ns\n");

            Random rand = new Random();
            for (int i = 0; i < iterations; i++) {
                // Generate random configurations
                double L = minL + rand.nextDouble() * minL*2;
                int N = minN + rand.nextInt(minN*2);
                double rc = minRc + rand.nextDouble() * minRc*9;
                int M = 1;

                // Create CellIndexMethod instances
                CellIndexMethod md = new CellIndexMethod(L, N, rc).generateRandomParticles();
                CellIndexMethod md2 = new CellIndexMethod(L, N, rc, M).generateRandomParticles();

                double runtimeDiff;

                // Alternate the order of execution
                if (rand.nextBoolean()) {
                    double runtime2 = (double) md2.runSimulation() /1_000_000;
                    double runtime = (double) md.runSimulation() /1_000_000;
                    runtimeDiff = runtime2 - runtime;
                } else {
                    double runtime = (double) md.runSimulation() /1_000_000;
                    double runtime2 = (double) md2.runSimulation() /1_000_000;
                    runtimeDiff = runtime2 - runtime;
                }
                System.out.println(runtimeDiff);

                // Record the result in the CSV file
                csvWriter.append(String.format("%d,%d,%f,%.2f,%f\n", i + 1, N, L, rc, runtimeDiff));
            }

            csvWriter.flush();
            csvWriter.close();

            System.out.println("Test completed. Results saved to " + fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createNeighborsFile(CellIndexMethod md) {
        try (FileWriter writer = new FileWriter("neighbors.txt")) {

            for (Integer id : md.getNeighbors().keySet()) {
                writer.write(String.format("%d\t", id));
                for (Particle particle : md.getNeighbors().get(id)) {
                    writer.write(String.format("%d ", particle.id));
                }
                writer.write("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createParticlesFile(CellIndexMethod md) {
        try (FileWriter writer = new FileWriter("static_particles.txt")) {
            writer.write(String.format("%d\n%f\n",md.N,md.L));
//            writer.write("id\tposx\tposy\n");
            for (Particle particle : md.getParticles()) {
                writer.write(String.format("%d\t%f\t%f\t%f\n", particle.id, particle.x, particle.y, particle.radius));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
