package cz.cvut.fel.stankmic.osp.osm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {
        if(args.length != 1) {
            System.err.println("Wrong number of arguments, expected 1 - PBF file.");
            System.exit(1);
        }
        final String fileName = args[0];
        if(!Files.exists(Paths.get(fileName))) {
            System.err.printf("File \"%1$s\" does not exist.\n", fileName);
            System.exit(2);
        }
        System.out.printf("Querying file: \"%1$s\".\n", fileName);

        final Map<String, Double> cache = new HashMap<>();
        final Scanner scanner = new Scanner(System.in);

        while(true) {
            System.out.print("Enter \"key=value\" query: ");
            final String query = scanner.nextLine().toLowerCase();
            if(cache.containsKey(query)) {
                System.out.println("Result found in cache.");
                printResult(query, cache.get(query));
            } else {
                System.out.println("Result not found in cache.");
                long queryStarted = System.currentTimeMillis();
                String[] input = query.split("=");
                PbfFileLengthCalculation calculations = new PbfFileLengthCalculation(fileName);
                double length = calculations.getLengthOfWays(input[0], input[1]);
                cache.put(query, length);
                printResult(query, length, queryStarted);
            }
            System.out.println("=====");
        }
    }

    private static void printResult(String query, double length) {
        System.out.printf("Total length of %1$s is %2$.3f km.\n", query, length/1000);
    }

    private static void printResult(String query, double length, long queryStarted) {
        printResult(query, length);
        long secondsElapsed = (System.currentTimeMillis()-queryStarted)/1000;
        System.out.printf("Time elapsed: %1$d m %2$d s.\n", (secondsElapsed)/60, (secondsElapsed)%60);
    }

}
