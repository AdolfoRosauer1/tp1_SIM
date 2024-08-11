import java.io.FileWriter;
import java.io.IOException;

public class runner {
    public static void main(String[] args) {

        double L = 50;
        int N = 5000;
        double rc = 3;

        CellIndexMethod md = new CellIndexMethod(L,N,rc)
                .generateRandomParticles()
                .generateIdealM();

        long runtime = md.runSimulation();
        double runtimeMilliseconds = runtime / 1_000_000.0;  // Convert to milliseconds
        System.out.println("Simulation runtime: " + runtimeMilliseconds + " milliseconds");

        createNeighborsFile(md);
        createParticlesFile(md);

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
        try (FileWriter writer = new FileWriter("static_particles.tsv")) {
            writer.write("id\tposx\tposy\n");
            for (Particle particle : md.getParticles()) {
                writer.write(String.format("%d\t%f\t%f\n", particle.id, particle.x, particle.y));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
