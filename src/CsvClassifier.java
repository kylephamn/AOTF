import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CsvClassifier {

    private static final String[] FIELDS = {
            "s1_v2_v2", "s2_v2_v2", "s3_v2_v2", "s4_v2_v2", "s5_v2_v2",
            "s6_v2_v2", "s7_v2_v2", "s8_v2_v2", "s9_v2_v2", "s10_v2_v2",
            "s11_v2_v2", "s12_v2_v2", "s13_v2_v2", "s14_v2_v2", "s15_v2_v2"
    };

    public static void main(String[] args) {
        int siteNumber = getSiteNumber();
        String inputFolder = "AOTF SURVEYS- NO DEMO";
        String outputFolder = "Output CSV";
        new File(outputFolder).mkdir(); // Ensure the output directory exists
        String inputFilePath = inputFolder + File.separator + "SITE" + siteNumber + "_PREIMP_RAW.csv";
        String outputFilePath = outputFolder + File.separator + "SITE" + siteNumber + "_PREIMP_RAW_output.csv";

        processCsvFile(inputFilePath, outputFilePath);
    }

    private static int getSiteNumber() {
        Scanner scanner = new Scanner(System.in);
        int siteNumber = 0;
        while (siteNumber < 1 || siteNumber > 4) {
            System.out.println("Enter the site number to process (1-4): ");
            if (scanner.hasNextInt()) {
                siteNumber = scanner.nextInt();
                if (siteNumber < 1 || siteNumber > 4) {
                    System.out.println("Invalid input. Please enter a number between 1 and 4.");
                }
            } else {
                System.out.println("Invalid input. Please enter a valid number between 1 and 4.");
                scanner.next(); // clear the invalid input
            }
        }
        return siteNumber;
    }

    private static void processCsvFile(String inputFilePath, String outputFilePath) {
        Map<String, int[]> summary = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
            String line = reader.readLine();
            if (line == null) {
                throw new IllegalArgumentException("CSV file is empty");
            }

            // Split headers
            String[] headers = line.split(",");

            // Initialize the summary map only for the specified fields
            for (String field : FIELDS) {
                summary.put(field, new int[3]); // [disagree, agree, neutral]
            }

            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                for (int i = 0; i < values.length; i++) {
                    if (i >= headers.length || !summary.containsKey(headers[i])) {
                        continue; // Skip columns not in the specified fields
                    }
                    String valueStr = values[i].trim();
                    if (valueStr.isEmpty()) {
                        continue; // skip empty values
                    }
                    try {
                        int value = Integer.parseInt(valueStr);
                        if (value == 1 || value == 2) {
                            summary.get(headers[i])[0]++;
                        } else if (value == 4 || value == 5) {
                            summary.get(headers[i])[1]++;
                        } else if (value == 3) {
                            summary.get(headers[i])[2]++;
                        }
                    } catch (NumberFormatException e) {
                        // Skip non-numeric values
                        continue;
                    }
                }
            }

            // Write the summary to a CSV file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
                // Write the header
                writer.write("Item Name,# Disagree,% Disagree,# Agree,% Agree,# Neutral,% Neutral\n");

                // Write the summary in the specified order
                for (String field : FIELDS) {
                    if (!summary.containsKey(field)) continue;

                    int[] counts = summary.get(field);
                    int total = counts[0] + counts[1] + counts[2];
                    double disagreePercent = (counts[0] * 100.0) / total;
                    double agreePercent = (counts[1] * 100.0) / total;
                    double neutralPercent = (counts[2] * 100.0) / total;

                    writer.write(String.format("%s,%d,%.2f%%,%d,%.2f%%,%d,%.2f%%\n",
                            field, counts[0], disagreePercent, counts[1], agreePercent, counts[2], neutralPercent));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
